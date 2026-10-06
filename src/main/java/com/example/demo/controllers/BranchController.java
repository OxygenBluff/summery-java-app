package com.example.demo.controllers;

import com.example.demo.dtos.BranchRequestDTO;
import com.example.demo.dtos.BranchResponseDTO;
import com.example.demo.mappers.BranchMapper;
import com.example.demo.repositories.BranchRepository;
import com.example.demo.repositories.SellerRepository;
import com.example.demo.services.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {
    private final SellerRepository sellerRepo;

    private final BranchRepository branchRepo;

    private final BranchMapper branchMapper;
    private final BranchService branchService;
    @GetMapping
    //map -> key = sellerId , value = Branch names
    public ResponseEntity<Map<Long,String>> fetchBranchesMap(){
        Map<Long,String> map = new HashMap<>();

        for(Object[] row: sellerRepo.findIdAndNomBoutique()){
            map.put((Long) row[0], (String) row[1]); // omg is this the thing before JPA ?

        }
        return ResponseEntity.ok(map);

        //TODO A BRANCH DTO best way ngl


    }

    //Now the actual list of the branches objects
    @GetMapping("/all")
    public ResponseEntity<List<BranchResponseDTO>> fetchBranchesList(){
        List<BranchResponseDTO> branches = branchRepo.findByActiveTrue().stream()
                .map(branchMapper::mapToBranchResponseDTO)
                .toList();

        return ResponseEntity.ok(branches);
    }

    //now by id for the branch screen
    @GetMapping("/{id}")
    public ResponseEntity<BranchResponseDTO> fetchBranch(
            @PathVariable Long id
    ){
        BranchResponseDTO branchFound =branchService.getBranch(id);
        return ResponseEntity.ok(branchFound);
    }

    //create admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchResponseDTO> createBranch(
            @Valid @RequestBody BranchRequestDTO request
    ){
        BranchResponseDTO createdBranch = branchService.createBranch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBranch(@PathVariable Long id){
        branchService.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }

}
