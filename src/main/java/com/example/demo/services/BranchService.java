package com.example.demo.services;


import com.example.demo.dtos.BranchRequestDTO;
import com.example.demo.dtos.BranchResponseDTO;
import com.example.demo.entities.Order;
import com.example.demo.entities.Seller;
import com.example.demo.entities.User;
import com.example.demo.entities.UserRole;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mappers.BranchMapper;
import com.example.demo.repositories.BranchRepository;
import com.example.demo.repositories.SellerRepository;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BranchService {
    private  final BranchRepository branchRepo;
    private final SellerRepository sellerRepo;

    private final BranchMapper branchMapper;
    private final UserRepository userRepo;


    //find by id
    @Transactional(readOnly = true)
    public BranchResponseDTO getBranch(Long id){
        Seller branch = sellerRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        return branchMapper.mapToBranchResponseDTO(branch);
    }

    //add put -> create a SELLER entity
    @Transactional
    public  BranchResponseDTO createBranch(BranchRequestDTO request){
        //FIND the seller in the DBBBB maybe it exists already ??
        //BY NAME cannnot extract id at this point ooh
        if(sellerRepo.existsByNomBoutique(request.getNomBoutique())){
            throw new IllegalStateException("a branch with that name already exists");
        }

        //OKAY WHO IS THE USER that already LOGGED IN with this email first btw
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("couldn't find a user with that email"));

        //PROMOTION
        user.setRole(UserRole.SELLER);

        Seller newBranch = branchMapper.mapToSellerEntity(request,user);
        Seller savedBranch = sellerRepo.save(newBranch);
        //LINKING
        user.setBranch(savedBranch);

        return branchMapper.mapToBranchResponseDTO(savedBranch);

    }

    @Transactional
    public void deleteBranch(Long id){
        if (!sellerRepo.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete: Branch not found with id: " + id);
        }
        //cascade ? the STAFF should not be deleted when branch(seller) is deleted
        sellerRepo.deleteById(id);

    }

}
