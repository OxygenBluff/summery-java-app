package com.example.demo.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductVariant {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	//many varaints (Ex: size S,L, XL..) -> ONE SAME product! 
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
	private Product product;
	//so now product -> many varients BUT
	// do we need productVariant.getCartItems() ? no.. oh

	
	private String attribut; //Ex: taille, color...
	private String valeur; // value de l'attribut 
	
	private Integer stockSupplementaire; 
	
	private Double prixDelta; //the + or - par rapport original !
	//imageUrl
	private String imageUrl;

	//hmm
	private boolean mandatory =false;
	

}
