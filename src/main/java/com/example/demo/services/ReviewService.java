package com.example.demo.services;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.entities.StatutCommande;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.ReviewRequestDTO;
import com.example.demo.dtos.ReviewResponseDTO;
import com.example.demo.entities.Product;
import com.example.demo.entities.Review;
import com.example.demo.entities.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mappers.ReviewMapper;
import com.example.demo.repositories.OrderItemRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ReviewService {
	//dpenedences
	private final ReviewRepository reviewRepository;
	private final OrderItemRepository orderItemRepository;
    
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;
    
    //new review
    public ReviewResponseDTO createReview(ReviewRequestDTO dto,User user) {
    	//review ONLY IF USER HAS PURCHISED THIS!
    	
    	boolean is_purchased=orderItemRepository.existsByOrder_Customer_IdAndProduct_IdAndOrder_Statut(
    			user.getId(),
    			dto.getProductId(),
                StatutCommande.PAID
        );
    	
    	if (!is_purchased) {
    		//TODO custom exception
    		throw new AccessDeniedException("You can only review products you have purchased.");
    	}
    	
    	//find product ->
    	Product p =productRepository.findById(dto.getProductId())
    			.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    	
    	// dto 
    	Review review = reviewMapper.toEntity(dto);
        review.setCustomer(user);
        review.setProduct(p);
        review.setApprouve(false); 
        review.setDateCreation(LocalDateTime.now());
        
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponseDTO(savedReview); // return DTO finally 
    }
    
    //supprimer review 
    public void deleteReview(Long id) {
    	if(!reviewRepository.existsById(id)) {
    		throw new ResourceNotFoundException("Review with id " + id +" not found");
    	}
    	reviewRepository.deleteById(id);
    }
    
    //APPRIVE by amdin -> transactional so. no need for save , Pre authorize in c
    @Transactional 
    public void approveReview(Long id) {
    	Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review with id " + id + " not found"));
    	review.setApprouve(true);
    }
    
    //find reviews by product... 
    //security context again for seller / admin
    //wait.. seller -> only if they sell the prouduct no ?
    //user -> also sees but only approved
    public List<ReviewResponseDTO> getReviewsByProduct(Long productId) {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String currentEmail = auth.getName();
    	
    	//find product -> find seller 
    	Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    	
    	//admin -> secuity contest, seller -> amtched prdoduct.getSeller 
    	boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwnerSeller = product.getSeller().getUser().getEmail().equals(currentEmail);
        //seller -> no direct email -> hop by User 
        
        List<Review> reviews;
        
        if (isAdmin || isOwnerSeller) {
        	//admin, seller -> see aprroved + non approved still
        	reviews = reviewRepository.findByProductId(productId);
        }else { 
        	reviews = reviewRepository.findByProductIdAndApprouveTrue(productId);
        	//only approved
        	
        }

        //dtos stream -> list 
        return reviews.stream()
                .map(reviewMapper::toResponseDTO)
                .toList();
    }
    
	
}










