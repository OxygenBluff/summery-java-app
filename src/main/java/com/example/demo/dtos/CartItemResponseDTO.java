package com.example.demo.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//cart item dto will be given back = > CART CONTENT 
//but with al NAMES + prices which are NOT in the request
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponseDTO {
	private Long id;
    private String productName;
    private List<String> variantNames; // many
    private Double unitPrice;
    private Integer quantity;
    private Double subTotal; //qte * prix uniatire 
    private String imageUrl; //oops..
    
    private List<CustomizationResponseDTO> customizations;

    // cart restrictions..
    private Long branchId;


}
