package com.example.demo.auth;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.SellerRegistrationDTO;
import com.example.demo.entities.Seller;
import com.example.demo.entities.User;
import com.example.demo.entities.UserRole;
import com.example.demo.repositories.SellerRepository;
import com.example.demo.repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")//JS AND SAVER might not be on the same port !
@RequiredArgsConstructor
public class AuthenticationController {
	//ENTRY POINT! 
	//controller uses service ofc 
	private final AuthenticationService service; 
	private final SellerRepository sellerRepository;
	private final UserRepository userRepo;
	
	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(
			@RequestBody RegisterRequest request
			){
		return ResponseEntity.ok(service.register(request));
		//what on earth is ResponseEntity.ok 
		//-> = helper, says if JSON staus is 200 ok :) 
	} 
	
	
	//THIS IS THE REAL JS JWT STUFF! 
	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(
			@RequestBody AuthenticationRequest request //MAPS THE USERNAME + EMAIL  to authenticationRequest!!
			){
		return ResponseEntity.ok(service.authenticate(request));
	}	
	
	
	
	//refreshing the token! otherwise user gets kicket out mid browsing after what was it 30 mins ? lol
	
	@PostMapping("/refresh-token")
	public void refreshToken(
			HttpServletRequest request,
	        HttpServletResponse response
			)throws IOException{
		service.refreshToken(request, response);
	}
	//TODO
	//why IO exception ?
	//-> this endpoint writes directly to the HTTP request/response STREAM !!
	//not the typical @ReQUESTbODY OR ResponseEntity !! doesn' go through
	//the Serialization thing
	
	
	@PostMapping("/sellers")
	public ResponseEntity<?> createSellerProfile(
	        @RequestBody SellerRegistrationDTO dto,
	        @AuthenticationPrincipal User user) {
	    
		
		Optional<Seller> existingSeller = sellerRepository.findByUserId(user.getId());
		
		if (existingSeller.isPresent()) {
	        return ResponseEntity.badRequest().body("You already have a seller profile!");
	    }
		
		//response -> bad request -> body .. my Gosh spring..
		
		 user.setRole(UserRole.SELLER);
		 userRepo.save(user);
		    
	    Seller seller = Seller.builder()
	            .user(user)
	            .nomBoutique(dto.getNomBoutique())
	            .description(dto.getDescription())
	            .logo(dto.getLogo())
	            .note(0.0)
	            .build();
	    
	    Seller saved = sellerRepository.save(seller);
	    
	    return ResponseEntity.ok().body(Map.of(
	            "message", "Seller profile created successfully!",
	            "shopName", seller.getNomBoutique()
	        ));
	}
	

}
