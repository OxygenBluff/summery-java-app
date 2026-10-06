package com.example.demo.entities;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	
	private String nom;
    private String description;
    private Double prix;
    
    private Double prixPromo; 
    private Integer stock;
    private boolean actif = true;
    private LocalDateTime dateCreation;
    
    //relation aves Seller: Many Products <-> One seller 
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="seller_id")
    private Seller seller;
    
    //relation M-M avec Category 
    @ManyToMany
    @JoinTable(
    		name="product_categories",
    		joinColumns= @JoinColumn(name="product_id"),
    		inverseJoinColumns=@JoinColumn(name="category_id")
    		)
    private List<Category> categories = new ArrayList<>();
    
    //Images 
    @ElementCollection 
    private List<String> images=new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
    	this.dateCreation=LocalDateTime.now();
    }
    
    //relation avec ProductVariant: 
    //ask a prodcut all its variants
    //mappedBy-> field name (private Product product)!!
    //many side has the fk -> mapped by it
    @OneToMany(mappedBy="product", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<ProductVariant> variants = new ArrayList<>();
    //can call product.getVariants() ! 
    //orphanRemoval -> if a variant no longer attached to child -> delete it 
    
    //relationship with customization! 
    //one product! -> customizations
    @OneToMany(mappedBy="product",cascade=CascadeType.ALL, orphanRemoval=true)
    private List <Customization> customizations= new ArrayList<>();

    //no reviews ?? what.. ok.. product -> many reviews
    @OneToMany(mappedBy="product",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    //this is extremely tricky ... -> computer lowest price TAKING ACCOUNT OF THE PROMO PRICE -> send it to the JPQL for the price filtering

    private Double lowestPrice;

   
}
	
	
	
	
	
	
	
