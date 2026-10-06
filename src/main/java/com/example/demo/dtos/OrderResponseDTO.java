package com.example.demo.dtos;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.entities.OrderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {
	private Long orderId;
    private LocalDateTime orderDate;

    //order number was missing.. ORD thingy
    private String numeroCommande;

    private String status; // ENUM StatutCommande TODO why string bruh
    private Double totalAmount;//Sum  : all subtotals ...
    
    private List<OrderItemResponseDTO> items;
    
    //new juice
    private OrderType orderType;
    private LocalDateTime pickupTime;
    private Long branchId;
    private String branchName;

    //dang it forgottt.
    private String deliveryAddress;

}
