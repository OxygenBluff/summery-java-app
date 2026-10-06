package com.example.demo.mappers;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.demo.dtos.CartItemResponseDTO;
import com.example.demo.dtos.OrderResponseDTO;
import com.example.demo.entities.Order;
import com.example.demo.entities.OrderItem;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class OrderMapper {
	
	private final OrderItemMapper orderItemMapper;
	
	//order entity -> OrderResponseDTO
	public OrderResponseDTO toResponseDTO(Order order) {
        if (order == null) return null;

		//the delivery address hello ?
        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .orderDate(order.getDateCommande())

				.numeroCommande(order.getNumeroCommande())

                .status(order.getStatut().toString())
                .totalAmount(order.getTotalTTC())
                //LEMON
                .branchId(order.getBranch() !=null ? order.getBranch().getId() : null)
                .branchName(order.getBranch() !=null ? order.getBranch().getNomBoutique() : null)

                .pickupTime(order.getPickupTime())
                
                .orderType(order.getOrderType())

				.deliveryAddress(order.getAdresseLivraison())
                
                .items(order.getLignes() != null ? 
                       order.getLignes().stream()
                            .map(orderItemMapper::toResponseDTO)
                            .collect(Collectors.toList()) : null)
                .build();
	}
	
	
	
	//NOT NEEDED
	/*
	//helper cartItem  -> cartItemResponseDTO
	private CartItemResponseDTO toCartItemResponseDTO(OrderItem item) {
        if (item == null) return null;

        // Using Builder for CartItemResponseDTO
        return CartItemResponseDTO.builder()
                .id(item.getId())
                .productName(item.getProduct().getNom())
                // variant = null ?
                .variantName(item.getVariant() != null ? item.getVariant().getAttribut() : "Standard")
                .unitPrice(item.getPrixUnitaire())
                .quantity(item.getQuantite())
                // sub total
                .subTotal(item.getPrixUnitaire() * item.getQuantite())
                .build();
    }

	 */
}
