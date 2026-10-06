package com.example.demo.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.OrderRequestDTO;
import com.example.demo.dtos.OrderResponseDTO;
import com.example.demo.entities.StatutCommande;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
	private final OrderService orderService;
    private final UserRepository userRepo;
    
    //WHICH USER ? again that holder..
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepo.findByEmail(email).orElseThrow();
    }
    
    //new order 
    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@RequestBody OrderRequestDTO request) {
        return ResponseEntity.ok(orderService.placeOrder(getCurrentUser(), request));
    }
    
    // GET /api/orders/my — commandes du client connecté

    //SORTING
    @GetMapping("/my")
    public ResponseEntity<Page<OrderResponseDTO>> getMyOrders(
            @PageableDefault(size=8, sort="dateCommande", direction = Sort.Direction.DESC)Pageable pageable
            ) {
        return ResponseEntity.ok(orderService.getMyOrders(getCurrentUser(),pageable));
    }
    
    //GET /api/orders/{id} — détail, Order Details , get Order
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }
    
    //change status : PUT /api/orders/{id}/status — mettre à jour le statut (SELLER/ADMIN)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Long id, @RequestParam StatutCommande status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
    
    //cancel order -> ADMIN , PUT /id/cancel 
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, getCurrentUser()));
    }
    
    //GET LIST of ALL orders.. dang no peageable ? this could crash the app no ?
    //I WILL MAKE IT PAEGABLE BUT IKD TODO ASK PROF
   
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getAll() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
    
    //NVM the API endpoint will cahnge.. 
    //apparentlty to this 
    //GET /api/orders?page=0&size=10&sort=dateCommande,desc
    //but i'll keep it here: 
    
    //ResponseEntity<Page<Order>> getAll
    // and     ResponseEntity.ok(orderRepo.findAll(pageable)
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
