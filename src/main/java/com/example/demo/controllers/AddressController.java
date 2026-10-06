package com.example.demo.controllers;

import java.util.List;

import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entities.Address;
import com.example.demo.entities.User;
import com.example.demo.repositories.AddressRepository;
import com.example.demo.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

	private final AddressRepository addressRepo;
	private final UserRepository userRepo;
	
	//i wonder why i haven't made this a helper already..
	
	private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
	
	//GET /api/addresses: all addtresses for the current user 
	@GetMapping("/me")
    public ResponseEntity<List<Address>> getMyAddresses() {
		User currentUser = getCurrentAuthenticatedUser();
		
        List<Address> addresses = addressRepo.findByUser(currentUser);

        //TODO List<AddressResponseDTO> dtos = addressMapper.toDTOList(addresses);
        return ResponseEntity.ok(addresses);
    }
	
	//POST: add a new user address api/addresses
	
	@PostMapping
    public ResponseEntity<Address> addAddress(@RequestBody Address address) {
		User currentUser = getCurrentAuthenticatedUser();

        //exists in DB check i HAD DUPLCIATES
        boolean addressExists = addressRepo.existsByUserAndRueIgnoreCaseAndVilleIgnoreCaseAndCodePostalIgnoreCaseAndPaysIgnoreCase(
                currentUser,
                address.getRue().trim(),
                address.getVille().trim(),
                address.getCodePostal().trim(),
                address.getPays().trim()
        );

        if(addressExists){
            throw new DuplicateResourceException("Address already exists.");

        }

        address.setUser(currentUser);
        
        //addresse par défaut = IF has no addresses principal let's just make it this one
        boolean hasExistingAddresses = addressRepo.existsByUser(currentUser);
        if(!hasExistingAddresses){
            address.setPrincipal(true);
        }
        
        Address saved = addressRepo.save(address);
        return ResponseEntity.ok(saved);

	}

    //i swear i added this before.. Delete an address
    @DeleteMapping("/me/{addressId}")
    public void deleteAddress(@PathVariable Long addressId){
        User currentUser = getCurrentAuthenticatedUser();
        //find it ofc
        Address address = addressRepo.findFirstByUser(currentUser)
                .orElseThrow(() -> new ResourceNotFoundException("couldn't find the address"));

        //wait delete DELETE
        addressRepo.deleteById(address.getId());


    }
}
















