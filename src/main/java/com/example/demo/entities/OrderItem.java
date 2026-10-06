package com.example.demo.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	//relation avec Order: M items <-> One order 
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
	@JsonBackReference // that huge bug.. -> JSON won't serialize it now!!
    private Order order;

	//relation avec produits de Order: 
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
	
	//relation avec variant: M variants <-> One order Item
	//Ex: item = hoodie in size S
	//50 different customers buy this -> in DB will have 50 OredrItem -> all point to the same product Variant!

	/*
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

	 */

	//updated
	@ManyToMany
	@JoinTable(
			name="order_item_variants",
			joinColumns = @JoinColumn(name = "order_item_id"),
			inverseJoinColumns = @JoinColumn(name="variant_id")
	)
	private List<ProductVariant> variants = new ArrayList<>();
	
	
	//Customization ONE EXTRA SYRUP PUMPPP! 
	//so one order -> many customizations list
	//BUTT.. with that custimization id can only belong to ONE only orderItem 
	//bruh just join table 
	
	
	@ManyToMany
	@JoinTable(
			name="order_item_customization",
			joinColumns= @JoinColumn(name="order_item_id"),
			inverseJoinColumns =@JoinColumn(name="customization_id")
		)
	private List<Customization> customizations = new ArrayList<>();
	
	private Integer quantite; 
	
	private Double prixUnitaire; 
	
	//SNAPSHOT customization cost -> stays frozen if prices change
	private Double customizationsCost;
	
}

