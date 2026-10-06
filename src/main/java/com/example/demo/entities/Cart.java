package com.example.demo.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	//relation avec user/customer 
	@OneToOne
	@JoinColumn(name="customer_id")
	private User customer;
	
	//relation avec Ligne = Cart Item , one cart many lignes
	@OneToMany(mappedBy="cart",cascade= CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CartItem> lignes = new ArrayList<>();
	
	private LocalDateTime dateModification;

	//where is the coupn persistance nahhh .. one coupon -> multiple carts
	@ManyToOne
	@JoinColumn(name="coupon_id")
	private Coupon appliedCoupon;

	
	@PreUpdate // = mettre à jour la date si cart change !! 
	@PrePersist
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }
	

}
