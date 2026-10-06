package com.example.demo.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	//relation avec User/customer: user peut avoir plusieurs commandes 
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;
	
	private LocalDateTime dateCommande;
	
	@Enumerated(EnumType.STRING)
    private StatutCommande statut;
	
	private String numeroCommande; //  ORD-2026-XXXXX thingy
	private String adresseLivraison;
	
	private Double sousTotal;
    private Double fraisLivraison;
    private Double totalTTC;
	
	
	//ask Order about its orderItem -> list: One order <-> Many Order Items right
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	@Builder.Default
	@JsonManagedReference //OMG BUG.. recusrsion!!!
	//THEN orderitem --> @JsonBackReference btw
    private List<OrderItem> lignes = new ArrayList<>();
	
	@PrePersist //once confirmed -> date saved EVEN if status changes!!
    protected void onCreate() {
        this.dateCommande = LocalDateTime.now();
        
        //WAIT -> status = null -> PENDING default ??
        //just safety because forget this with builder and it will be null
        //but pending i guess ?
        if (this.statut == null) {
            this.statut = StatutCommande.PENDING;
        }
    }
	
	
	//Juice so.. pickup in the shop OR also delivery i mean why not..
	//which branch (seller) is handling this order
	@ManyToOne
	@JoinColumn(name="branch_id")
	private Seller branch;
	
	//CHOOSE PICKUP 
	private LocalDateTime pickupTime;
	
	@Enumerated(EnumType.STRING)
	private OrderType orderType;
	//THIS DEFINES! PICKUP or DELIVERY order! 
	

}
