package com.example.demo.controllers;

import com.example.demo.auth.AuthenticationController;
import com.example.demo.dtos.UpdateUserRequestDTO;
import com.example.demo.dtos.UserResponseDTO;
import com.example.demo.entities.User;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController { //me
    private final UserService userService;
    private final UserMapper userMapper;
    private final UserRepository userRepo;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUserProfile(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(userMapper.toDTO(currentUser));
    }

    //partial updates = PATCH MAPPING wth ?
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUserProfile(
            Authentication auth,
            @RequestBody UpdateUserRequestDTO dto
    ){
        String email = auth.getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponseDTO response = userService.updateProfile(
            currentUser,dto
        );
        return ResponseEntity.ok(response);
    }




}
