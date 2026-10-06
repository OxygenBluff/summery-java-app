package com.example.demo.controllers;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.AdminDashboardDTO;
import com.example.demo.dtos.CustomerDashboardDTO;
import com.example.demo.dtos.SellerDashboardDTO;
import com.example.demo.entities.User;
import com.example.demo.services.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
	private final DashboardService dashboardService;

	//admin
	@GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }
	
	//seller
	@GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SellerDashboardDTO> getSellerDashboard(@AuthenticationPrincipal User user) {
        // userPrincipal.getId() is the User's ID linked to the Seller
        return ResponseEntity.ok(dashboardService.getSellerDashboard(user.getId()));
    }
	
	//idk why i added user oh well
	@GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerDashboardDTO> getCustomerDashboard(@AuthenticationPrincipal User user) {
        // We'll need to fetch the User entity or just pass the ID if your service allows
        return ResponseEntity.ok(dashboardService.getCustomerDashboard(user.getId()));
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
