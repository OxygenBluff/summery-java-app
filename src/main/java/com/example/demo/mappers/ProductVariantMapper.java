package com.example.demo.mappers;

import org.springframework.stereotype.Component;

import com.example.demo.dtos.VariantRequestDTO;
import com.example.demo.dtos.VariantResponseDTO;
import com.example.demo.entities.ProductVariant;

@Component
//why EVERY mapper is @Component ?
//-> spring creates ONE INSTANCE of it and keeps it in ApplicationCOntext !!
//-> + depenendency easy injection :) 

//JUST a RQ: @Bean -> for code NOT YOURS = librbaries can't be @Compoennet there!

public class ProductVariantMapper {
	//function1: Variant Request dto -> Product Variant
	public ProductVariant toEntity(VariantRequestDTO dto) {
		if (dto == null) return null;
		ProductVariant entity = new ProductVariant();
		entity.setAttribut(dto.getAttribut());
        entity.setValeur(dto.getValeur());
        entity.setPrixDelta(dto.getPrixDelta());
        entity.setStockSupplementaire(dto.getStockSupplementaire());
        entity.setImageUrl(dto.getImageUrl());
        return entity;
	}
	
	//function 2: Product Vairant/ entity -> VariantResponse dto 
	public VariantResponseDTO toResponseDTO(ProductVariant entity) {
	if (entity == null) return null;
	return VariantResponseDTO.builder()
			.id(entity.getId())
            .attribut(entity.getAttribut())
            .valeur(entity.getValeur())
            .prixDelta(entity.getPrixDelta())
            .stockSupplementaire(entity.getStockSupplementaire())
			.mandatory(entity.isMandatory())
            .imageUrl(entity.getImageUrl())
            .build();
	}
}
