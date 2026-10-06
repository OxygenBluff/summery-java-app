package com.example.demo.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemRequestDTO {
	private Long productId;
    private List<Long> variantIds; // many
    private Integer quantity;
    
    private List<Long> customizationIds;
}

//what the helly is CartItem vs cart ??
//well, when viewing a product and clicking add to cart
//-> font end sends THIS: CartItemRequest -> productId + quantity ONLY
//Ex: { productId: 5, quantity: 1 }

//controller receives it -> hands it to CartService 
//creates CartItem ENTITY in the cart entity 
//-> saved the cart 
//-> then ofc cart enetity -> cartResponseDTO for the pretty front end c*nty LMAO i lost it 

//Rq: Cart = CONTAINER -> to ONE user only, => TotalPrice 
//CartItem -> IDDIVIDUAL LINES : Ex, S25 x2 
//another line Hoodie  x5 ... 
