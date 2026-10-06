package com.example.demo.mappers;


import com.example.demo.dtos.UserResponseDTO;
import com.example.demo.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    //user entity -> response DTO
    public UserResponseDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .firstName(user.getNom())
                .lastName(user.getPrenom())
                .email(user.getEmail())
                .active(user.isActif())
                .createdAt(user.getDateCreation())
                .build();
    }

    //UPDATE REQUEST

}
