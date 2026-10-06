package com.example.demo.dtos;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponseDTO {

	private Long id;
    private String productName;
    private List<VariantResponseDTO> variants;
    private Double unitPrice;
    private Double customizationsCost;
    private Integer quantite;
    private Double subTotal;         // (unitPrice + customizationsCost) * quantite
    private String imageUrl;         // nice to have!
    private List<CustomizationResponseDTO> customizations;
}
