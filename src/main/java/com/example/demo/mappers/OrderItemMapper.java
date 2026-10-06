package com.example.demo.mappers;

import com.example.demo.entities.ProductVariant;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.OrderItemResponseDTO;
import com.example.demo.entities.OrderItem;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class OrderItemMapper {
	private final CustomizationMapper customizationMapper;

	private final ProductVariantMapper productVariantMapper;
	
	//entity OrderItem -> OrderItemResponseDTO
	  public OrderItemResponseDTO toResponseDTO(OrderItem item) {
	        if (item == null) return null;
	        return OrderItemResponseDTO.builder()
	                .id(item.getId())
	                
	                .productName(item.getProduct().getNom())
	                
	                .variants(item.getVariants().stream()
							.map(productVariantMapper::toResponseDTO)
							.toList()
					)
	                
	                .unitPrice(item.getPrixUnitaire())
	                
	                .customizationsCost(item.getCustomizationsCost())
	                
	                .quantite(item.getQuantite())
	                
	                .subTotal(item.getPrixUnitaire()
	                    * item.getQuantite())
	                
	                .customizations(item.getCustomizations().stream()
	                    .map(customizationMapper::toResponseDTO)
	                    .toList())
	                
	                .build();
	    }
	

}
