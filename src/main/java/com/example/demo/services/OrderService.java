package com.example.demo.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.demo.entities.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.OrderRequestDTO;
import com.example.demo.dtos.OrderResponseDTO;
import com.example.demo.mappers.OrderMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
	
	private final CartRepository cartRepo;
	private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final AddressRepository addressRepo;
    
    private final OrderMapper orderMapper;
    
    private final SellerRepository sellerRepo;
	private final UserRepository userRepo;

	private final ProductVariantRepository variantRepo;
   
	
	//format de commande ORD-2026-XXXXX
	public String generateOrderNumber() {
		return "ORD-" + LocalDate.now().getYear()+"-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
		
	}//whh is UUID ?
	//-> UNIVERSALLY guaranteed 128bit number across rhe world.. 
	//how do they even do that wth..

	//NEW STOCK
	public int calculateStock(Product product, List<ProductVariant> variants){
		if(variants!=null && !variants.isEmpty()){
			//MIN
			return variants.stream()
					.mapToInt(ProductVariant::getStockSupplementaire)
					.min()
					.orElse(product.getStock());
		}
		return product.getStock(); // empty
	}
	
	
	//place order 
	@Transactional 
	public OrderResponseDTO placeOrder(User customer,OrderRequestDTO request) {
		//user cart 
		Cart cart = cartRepo.findByCustomer(customer)
	            .orElseThrow(() -> new RuntimeException("Cart is Empty"));
		//TODO CUSTOM EXCEPTION
		
		if (cart.getLignes().isEmpty()) throw new RuntimeException("Cart is Empty");
		
		//addresse doit etre valide! !
		//LEMOANDE! address optional only for delivery not pickup 
		String DeliveryAddress=null; 
		//BY DEFAULT null = pickup else it just changes here
		
		if(request.getOrderType() == OrderType.DELIVERY) {
			Address address = addressRepo.findById(request.getAddressId())
		            .orElseThrow(() -> new RuntimeException("Invalid Address"));
			
			DeliveryAddress=address.getRue() +", " + address.getVille() + " " + address.getCodePostal();
					
		}
		//TODO CUSTOM EXCEPTIONS
		
		//new order finally..
		
		Order order = Order.builder()
	            .customer(customer)
	            .numeroCommande(generateOrderNumber())
	            //address livrasion -> rue,ville,code postal i guess
	            .adresseLivraison(DeliveryAddress)
	            .orderType(request.getOrderType())
	            .pickupTime(request.getPickupTime())
	            .branch(sellerRepo.findById(request.getBranchId())
	            	    .orElseThrow(() -> new RuntimeException("Branch not found")))
	            .statut(StatutCommande.PENDING)
	            .lignes(new ArrayList<>()) //new list Builder. soemthing thingy
	            .fraisLivraison(request.getOrderType() == OrderType.DELIVERY ? 7.0 : 0.0) // ?? min 7DT .. TODO what is it?
	            .dateCommande(LocalDateTime.now())//PRE PERSIST DOESNT DO IT ?
	            .build();
		
		double runningSubTotal = 0;
		
		//Cart -> many CartItems (lignes) EACH IS A PRODUCT!
		for (CartItem cartItem : cart.getLignes()) {
			Product product = cartItem.getProduct();


			//stock enough ?
			//TODO FIX -> Min
			int remainingStock = calculateStock(cartItem.getProduct(),cartItem.getVariants());

			if (remainingStock< cartItem.getQuantite()) {
	            throw new RuntimeException("Insufficient Stock for the Product: " + product.getNom());
	        }
			
			//else subtract stock:  + SAVE AGAIN
			//TODO, also must subtract the VARIANT STOCK SUPPLEMENTAIRE IF no variant

			//CASE 1: ITEM WITH VARIANTS
			if(cartItem.getVariants() !=null && !cartItem.getVariants().isEmpty()){
				for(ProductVariant variant: cartItem.getVariants()){
					//so variant exists, subtract from the cart qte still ..
					//what is variant not mandatory ? oh ??
					//TODO PELASEE investigate
					//TODO
					variant.setStockSupplementaire(variant.getStockSupplementaire()-cartItem.getQuantite());
					variantRepo.save(variant);
				}
			}else{
				//NO VARIANTS -> - baseStock
				product.setStock(product.getStock()- cartItem.getQuantite());
				productRepo.save(product);
			}
			
	        
	        //all good ? -> OrderItem = snapshot , doesn't change!

			//variant detlas price increase with order
			double variantsDelta = cartItem.getVariants().stream()
					.mapToDouble(variant -> variant.getPrixDelta() != null ? variant.getPrixDelta(): 0.0)
					.sum();

			double customizationsDelta = cartItem.getCustomizations().stream()
					.mapToDouble(Customization::getExtraPrice)
					.sum();


			//DISCOUNTSSS
			double effectivePrice = product.getPrixPromo() !=null ? product.getPrixPromo() : product.getPrix();

	        OrderItem orderItem = OrderItem.builder()
	                .order(order)
	                .product(product)
	                .variants(cartItem.getVariants())
	                .quantite(cartItem.getQuantite())
					//TODO variant detlassss
	                .prixUnitaire(effectivePrice+variantsDelta + customizationsDelta) // PRICE IS FROZEN AND SET§§
	                .customizations(cartItem.getCustomizations()) // SAME FROM CART!!
	                .customizationsCost(cartItem.getCustomizations().stream()
	                        .mapToDouble(Customization::getExtraPrice)
	                        .sum())
	                .build();
	        
	        //-> add to order 
	        order.getLignes().add(orderItem);
	        runningSubTotal += orderItem.getPrixUnitaire() * orderItem.getQuantite();
			
		}
		//Discount application WHERE ??
		double discount = 0.0;
		Coupon appliedCoupon = cart.getAppliedCoupon();
		if(appliedCoupon!=null){
			//1 types: FIXED or PERCENT
			if( appliedCoupon.getType().equalsIgnoreCase("FIXED")){
				discount=appliedCoupon.getValeur();
			}else if(appliedCoupon.getType().equalsIgnoreCase("PERCENT")){
				discount=(appliedCoupon.getValeur()/100.0)*runningSubTotal;
			}
		}

		discount = Math.min(runningSubTotal,discount);
		double finalTotal = (runningSubTotal - discount);

		order.setSousTotal(finalTotal);
		//TODO.. might need to set this to the original running subtotal..
		//THEN add a column discounted for RECORDS
	    order.setTotalTTC(finalTotal + order.getFraisLivraison());
	    
	    //if order saved -> cart is wiped I FORGOT! 
	    Order savedOrder = orderRepo.save(order);
	    cart.getLignes().clear(); // Orphan removal handles the DB cleanup
		//also coupon
		cart.setAppliedCoupon(null);
	    cartRepo.save(cart);
	    
	    return orderMapper.toResponseDTO(savedOrder);
		
	}
	
	//finally rest of API endpoints. 
	
	//get order by id
	//TODO vulnerability ...  admin can get without no check, BUT user ? match your id

	public OrderResponseDTO getOrderById(Long id) {

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User currentUser= userRepo.findByEmail(email)
				.orElseThrow(()-> new ResourceNotFoundException("User not found"));

		Order order = orderRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found"));

		//SECURITYYYY
		boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

		boolean isOrderOwner = order.getCustomer().getId().equals(currentUser.getId());

		if(!isOrderOwner && !isAdmin){
			//not the owner, then u must be an admin, oh r neither ? bye bye
			throw new AccessDeniedException("Access denied for this request");
		}

		return orderMapper.toResponseDTO(order);
	}
	
	// GET -> /api/orders/my all orders
	//UPDATED TO PAGE
	public Page<OrderResponseDTO> getMyOrders(User customer, Pageable pageable) {

		return orderRepo.findByCustomer(customer,pageable)
				.map(orderMapper::toResponseDTO);
		//no need to stream and collect for pages oh wew
	}
	
	// PUT /api/orders/{id}/status -> update order status 
	public OrderResponseDTO updateStatus(Long id, StatutCommande newStatus) {
		Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatut(newStatus);
        
        Order savedOrder = orderRepo.save(order);
        
        return orderMapper.toResponseDTO(savedOrder);
	}
	
	//post cancel by id  /api/orders/{id}/cancel
	//which ones can be cancelled ?
	//YOUR commande + PENDING or PAID status otherwise no..
	//+ ADD BACK TO STOCK if successful cancellation! 
	
	public OrderResponseDTO cancelOrder(Long id, User customer) {
		Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
	    
	    //yours
	    if (!order.getCustomer().getId().equals(customer.getId())) {
	        throw new RuntimeException("Cannot Cancel Order.");
	    }
	    
	    //PENDING + PAID
	    if (order.getStatut() != StatutCommande.PENDING && order.getStatut() != StatutCommande.PAID) {
	        throw new RuntimeException("Cannot Cancel Order Currently.");
	    }
	    //TODO CUSTOM EXCPETIONS !!
	    
	    //+ stock -> order.getQuantite, for each Ligne (item) in order
	    for (OrderItem ligne : order.getLignes()) {
	        Product p = ligne.getProduct();
	        p.setStock(p.getStock() + ligne.getQuantite());
	        productRepo.save(p);
	    }
	    
	    order.setStatut(StatutCommande.CANCELLED);
	    
	    Order savedOrder = orderRepo.save(order);
	    
	    return orderMapper.toResponseDTO(savedOrder);
	}
	
	//ADMIN, all orders, GET /api/orders
	public List<OrderResponseDTO> getAllOrders() {
		return orderRepo.findAll()
	            .stream()
	            .map(orderMapper::toResponseDTO)
	            .collect(Collectors.toList());
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
