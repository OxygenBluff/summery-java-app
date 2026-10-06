package com.example.demo.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequestDTO {
    //can update: names + email password its onw seperate thing..
    private String firstName;
    private String lastName;

    @Email(message="Invalid email format")
    private String email;
}
