package com.example.demo.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserResponseDTO {
    private String firstName;
    private String lastName;
    private String email;


    private boolean active;
    private LocalDateTime createdAt;

    private String accessToken;
    private String refreshToken;

}
