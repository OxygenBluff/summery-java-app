package com.example.demo.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductResponseDTO {
	private Long id;
    private String nom;
    private String description;
    private Double prix;
    private Double prixPromo;
    private Integer stock;
    private boolean actif;
    private LocalDateTime dateCreation;
    private List<String> images;
    private List<CategoryResponseDTO> categories; 
    private List<VariantResponseDTO> variants;
    private Long sellerId;
    
    private Double noteMoyenne; // PRDUCT SERVICE NEEDS
    private List<ReviewResponseDTO> reviews; // service needs .. in prouct service again..
    
    //customizations EXTRA SYRUP PUMPPPP!
    private List<CustomizationResponseDTO>  customizations;

    //sh..
    private Double lowestPrice;
}
