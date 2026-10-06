package com.example.demo.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entities.*;
import org.springframework.stereotype.Component;

import com.example.demo.dtos.CartItemResponseDTO;
import com.example.demo.dtos.CartResponseDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final CustomizationMapper customizationMapper;

	//from cart Entity -> cart response DTO 
	public CartResponseDTO toResponseDTO(Cart cart) {
		if (cart==null) return null; 
		
		CartResponseDTO dto = new CartResponseDTO();
		dto.setId(cart.getId());
		
		//each item inside cart entitty -> to CartItemResponse dto 
		List<CartItemResponseDTO> itemDTOs = cart.getLignes().stream()
				.map(this::toCartItemResponseDTO ) // this = current MAPPER instance ofc
				
				.collect(Collectors.toList());
		
		dto.setItems(itemDTOs);
		
		//cart -> total ofc 
		double total = itemDTOs.stream()
				.mapToDouble(CartItemResponseDTO::getSubTotal)
				.sum(); 
		
		dto.setTotalCartPrice(total);

		//NOW the coupon -> get it from the DB ?
		double discount =0.0;
		String code = null;

		if(cart.getAppliedCoupon()!=null){
			Coupon coupon = cart.getAppliedCoupon();
			code=coupon.getCode();

			//PERCENT
			if(coupon.getType().equalsIgnoreCase("PERCENT")){
				discount=total *(coupon.getValeur()/100.0);
			}else if(coupon.getType().equalsIgnoreCase("FIXED")){
				discount=coupon.getValeur();
			}
			discount=Math.min(discount,total);//MIN discount ever = the price so it becomes 0 looool i actually forgot omg

		}

		dto.setAppliedCouponCode(code);
	    dto.setDiscountAmount(discount);
	    dto.setFinalPrice(total-discount);
		
		return dto;
				
	}
	
	//Cart ITEM -> CartItemResponseDTO
	private CartItemResponseDTO toCartItemResponseDTO (CartItem item) {
		CartItemResponseDTO dto = new CartItemResponseDTO(); 
		//id, productName, qte 
		
		dto.setId(item.getId());
        dto.setProductName(item.getProduct().getNom());
        dto.setQuantity(item.getQuantite());

		dto.setBranchId(item.getProduct().getSeller().getId());
        
       //price delta = diff right ?
		//TODO DISCOUNTED OMG..
        double price = item.getProduct().getPrixPromo() !=null
				? item.getProduct().getPrixPromo()
				: item.getProduct().getPrix();
        
        //if variant -> subtraction  i guess ..
        //updated all the sum
		double variantsPriceDelta = item.getVariants().stream()
				.mapToDouble(variant -> variant.getPrixDelta() != null ? variant.getPrixDelta() : 0.0)
				.sum();
		price += variantsPriceDelta;

        
        
        //what if no iamges ? -> fall back!!
		//removed
		/*
        if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
            dto.setImageUrl(item.getProduct().getImages().get(0));
        }else {
        	dto.setImageUrl("/images/placeholder-product.png");
        }

		 */

		dto.setImageUrl(item.getProduct().getImages().isEmpty() ? null : item.getProduct().getImages().get(0));
        
        dto.setVariantNames(item.getVariants().stream()
						.map(ProductVariant::getValeur)
						.toList());
        
        //customizations ADD PRICE! 
        if(item.getCustomizations() != null && !item.getCustomizations().isEmpty()) {
        	double customizationsCost = item.getCustomizations().stream()
        			.mapToDouble(Customization::getExtraPrice)
        			.sum();
        	price+=customizationsCost;
        }
        
        dto.setUnitPrice(price);
        dto.setSubTotal(price * item.getQuantite());
        
        dto.setCustomizations(
        	    item.getCustomizations() != null ?
        	    item.getCustomizations().stream()
        	        .map(customizationMapper::toResponseDTO)
        	        .toList() : new ArrayList<>()
        	);
        
        return dto;

	}
	

}
