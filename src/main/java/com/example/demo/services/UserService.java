package com.example.demo.services;


import com.example.demo.auth.AuthenticationResponse;
import com.example.demo.auth.AuthenticationService;
import com.example.demo.dtos.UpdateUserRequestDTO;
import com.example.demo.dtos.UserResponseDTO;
import com.example.demo.entities.User;
import com.example.demo.mappers.UserMapper;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final UserMapper userMapper;

    private final AuthenticationService authService;
    private final JwtService jwtService;

    //update user information
    public UserResponseDTO updateProfile (User user, UpdateUserRequestDTO dto){
        //NOT NULL = update it otherwise KEEP THE CURRENT USER VALUE
        String newFirstName = (dto.getFirstName()!=null && !dto.getFirstName().isBlank())
                ? dto.getFirstName().trim() : user.getNom();

        String newLastName = (dto.getLastName()!=null && !dto.getLastName().isBlank())
                ? dto.getLastName().trim() : user.getPrenom();

        user.setNom(newFirstName);
        user.setPrenom(newLastName);

        //EMAIL CHECKKK must be unqiue could crash the db
        if(dto.getEmail()!=null && !dto.getEmail().isBlank()){
            String newEmail = dto.getEmail().trim().toLowerCase();

            if(!newEmail.equalsIgnoreCase(user.getEmail()) && userRepo.existsByEmail(newEmail)){
                throw new IllegalArgumentException("This Email is already taken");
            }else {
                user.setEmail(newEmail);
            }
        }
        //return the neww user
        User savedUser = userRepo.save(user);

        //well now .. new tokens ?
        //revoke + new
        AuthenticationResponse newTokens = authService.reissueTokens(savedUser);

        //dto
        UserResponseDTO responseDTO = userMapper.toDTO(savedUser);
        responseDTO.setAccessToken(newTokens.getAccessToken());
        responseDTO.setRefreshToken(newTokens.getRefreshToken());

        return responseDTO;
    }
}
