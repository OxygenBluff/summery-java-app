package com.example.demo.dtos;


import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
public class BranchResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String logo; //URL ofc

    private String address;
    private String city;

    private Double latitude;
    private Double longitude;

    private String phone;
    private String email;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private Boolean active;
    private Double rating;

    private List<ProductResponseDTO> products;

}
