package com.example.demo.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

//admin needs to add them after all..
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class BranchRequestDTO {
    @NotBlank(message="branch name is required")
    private String nomBoutique;

    @NotBlank(message="branch address is required")
    private String address;

    @NotBlank(message="branch city is required")
    private String city;

    private String description;
    private String logo;

    private Double latitude;
    private Double longitude;

    @NotBlank(message="branch phone number is required")
    private String phone;
    private String email;

    private LocalTime openingTime;
    private LocalTime closingTime;

    private Boolean active;

}
