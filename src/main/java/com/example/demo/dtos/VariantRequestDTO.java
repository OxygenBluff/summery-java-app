package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor  
@AllArgsConstructor
public class VariantRequestDTO {
	private String attribut;        // like "taille"
    private String valeur;          // like "XL"
    private Integer stockSupplementaire; 
    private Double prixDelta;

    private boolean mandatory = false;
    private String imageUrl;

}
