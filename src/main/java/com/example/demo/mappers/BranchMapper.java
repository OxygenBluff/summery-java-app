package com.example.demo.mappers;

import com.example.demo.dtos.BranchRequestDTO;
import com.example.demo.dtos.BranchResponseDTO;
import com.example.demo.entities.Seller;
import com.example.demo.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BranchMapper {
    //seller entity -> branch DTO
    //list empty i guess so.. getting away with no pageable result hehehe
    public BranchResponseDTO mapToBranchResponseDTO(Seller seller){
        return BranchResponseDTO.builder()
                .id(seller.getId())
                .name(seller.getNomBoutique())
                .description(seller.getDescription())
                .logo(seller.getLogo())

                .address(seller.getAddress())
                .city(seller.getCity())

                .latitude(seller.getLatitude())
                .longitude(seller.getLongitude())

                .phone(seller.getPhone())
                .email(seller.getEmail())

                .openingTime(seller.getOpeningTime())
                .closingTime(seller.getClosingTime())

                .active(seller.isActive())

                .rating(seller.getNote())
                .products(null)
                .build();
    }


    //branch DTO -> Seller entity
    public Seller mapToSellerEntity(BranchRequestDTO dto, User owner){
        return Seller.builder()
                .user(owner)
                .nomBoutique(dto.getNomBoutique())
                .description(dto.getDescription())
                .logo(dto.getLogo())
                .address(dto.getAddress())
                .city(dto.getCity())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .openingTime(dto.getOpeningTime())
                .closingTime(dto.getClosingTime())
                .active(dto.getActive() != null ? dto.getActive() : true)
                //STUPID BUULDER NULL BECOMES TRUE
                .note(0.0)
                .build();
    }
}
