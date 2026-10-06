package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor  
@AllArgsConstructor
public class VariantResponseDTO {
		private Long id; //dang it
	    private String attribut;
	    private String valeur;
	    private Double prixDelta;
	    private Integer stockSupplementaire;

		private Boolean mandatory;

        private String imageUrl;
}

