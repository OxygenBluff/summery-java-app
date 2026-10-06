package com.example.demo.entities;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Seller {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id; 
	
	@OneToOne
	@JoinColumn(name="user_id",nullable = true)
	private User user; // 1-1 User: Seller
	
	private String nomBoutique; 
	private String description; 
	private String logo; 
	
	
	//physical juice chain shop so... 
	private String address;
	private String city;
	private Double latitude; 
	private Double longitude;
	
	private String phone;
	private String email;
	
	//open hours !? date data type ? string is fineee nah it's LocalTime
	private LocalTime openingTime;
	private LocalTime closingTime;

	@Builder.Default
	@Column(columnDefinition = "boolean default true")
	//it's the same weird thing.. this annotation otherwise in the DB it will be null..
	private boolean active = true;

	//MF THIS STILL DOES NOT MAKE IT TRUE ,????  just tells it what it is.
	//this is how to: @Builder.Default
	
	
	private Double note; //rating 
	
	
	//staff ?
	@OneToMany(mappedBy="branch")
	private List<User> staff = new ArrayList<>();
	//delete branch -> DO  NOT DELETE THE USERS! no cascade pheww 
	
	//give all seller's products !!
	@OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();
	
	//cascadeType All -> create new products to the seller -> all will be automtaically saved in ONE GO!
	//OTHERWISE repo.save(product1) -> repo.save(product2) ... W H A T  ????
	//AND if you forget one -> ERROR! it will see a product not linked to a seller ! 
	
	
	//-> cascadeAll 
	//1-Create Seller object 
	//2-Add prodicts -> List<Product>
	
	//3-sellerRepo.save(mySeller) [POV of DB manager! take this Seller and put it in the Seller table that's IT]
}
