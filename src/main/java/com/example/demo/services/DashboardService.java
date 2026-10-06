package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dtos.AdminDashboardDTO;
import com.example.demo.dtos.CustomerDashboardDTO;
import com.example.demo.dtos.OrderResponseDTO;
import com.example.demo.dtos.ProductResponseDTO;
import com.example.demo.dtos.ReviewResponseDTO;
import com.example.demo.dtos.SellerDashboardDTO;
import com.example.demo.entities.Seller;
import com.example.demo.entities.StatutCommande;
import com.example.demo.mappers.OrderMapper;
import com.example.demo.mappers.ProductMapper;
import com.example.demo.mappers.ReviewMapper;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.repositories.ReviewRepository;
import com.example.demo.repositories.SellerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {
	private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final SellerRepository sellerRepo;
    
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    
    private final ProductService productService; //HAS TOP 10 PRODUCTS ALREADY!!
    

    private final ReviewMapper reviewMapper;
    
    private final ReviewRepository reviewRepo;
    
    
    //1-ADMIN, revenue,recent orders,top products
    
    public AdminDashboardDTO getAdminDashboard() {
    	Pageable limitFive = PageRequest.of(0, 20); // this is how to set a pager for 20 YES
    	//U CAN PRE LIMIT IT!!
    	
    	//-chiffre d'affaires:
    	//somme de TotalTTC + statut = paid, also shipped ..
    	Double revenue = orderRepo.sumPlatformRevenue(
    			List.of(StatutCommande.PAID, StatutCommande.DELIVERED)
    			);
    	
    	//-recent commandes
    	List<OrderResponseDTO> recent = orderRepo.findAllByOrderByDateCommandeDesc(limitFive)
                .stream().map(orderMapper::toResponseDTO).toList();
    		
    	//top 10 selling products 
    	List<ProductResponseDTO> tops = productService.getTopSelling();
    	
    	return  AdminDashboardDTO.builder()
        .globalRevenue(revenue != null ? revenue : 0.0)
        .recentOrders(recent)
        .topProducts(tops) 
        .topSellers(new ArrayList<>()) // for now JUST TOP 10
        .build();
    	
    }
    
    //2-Seller dashboard: 
    //ID NOT USER
    public SellerDashboardDTO getSellerDashboard(Long id) {
    	Seller seller = sellerRepo.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Seller profile not found"));
    	
    	Double revenue = orderRepo.sumSellerRevenue(
    			seller.getId(),
    			List.of(StatutCommande.PAID, StatutCommande.DELIVERED)
    			);
    	
    	return SellerDashboardDTO.builder()
                .revenue(revenue != null ? revenue : 0.0)
                .pendingOrdersCount(orderRepo.countOrdersBySellerIdAndStatut(seller.getId(),StatutCommande.PENDING))
                
                .lowStockAlerts(productRepo.findBySellerAndStockLessThanEqual(seller, 5)
                		//ID better..
                        .stream().map(productMapper::toResponseDTO).toList())
                .build();
    }
    
    //3-Customer borad: 
    public CustomerDashboardDTO getCustomerDashboard(Long customerId) {
    	
    	//-current orders 
    	List<OrderResponseDTO> currentOrders = orderRepo.findByCustomerIdOrderByDateCommandeDesc(customerId)
                .stream()
                .filter(o -> o.getStatut() == StatutCommande.PENDING || o.getStatut() == StatutCommande.DELIVERED)
                .map(orderMapper::toResponseDTO)
                .toList();
    	
    	// latest reviews given
    	Pageable limitThree = PageRequest.of(0, 3);
        List<ReviewResponseDTO> recentReviews = reviewRepo.findByCustomerIdOrderByDateCreationDesc(customerId, limitThree)
                .stream()
                .map(reviewMapper::toResponseDTO)
                .toList();
        
        return CustomerDashboardDTO.builder()
                .currentOrders(currentOrders)
                .latestReviews(recentReviews)
                .build();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
