package com.example.demo.auth;

import java.io.IOException;

import com.example.demo.dtos.UserResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entities.Token;
import com.example.demo.entities.TokenType;
import com.example.demo.entities.User;
import com.example.demo.entities.UserRole;
import com.example.demo.exception.InvalidRefreshTokenException;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repositories.TokenRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor //same as autowired.. -> Lombok creates full constructor -> Spring injects it automatically 
public class AuthenticationService {
	private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; 
    private final AuthenticationManager authenticationManager;
    //why private final ? ="MUST be provided when this class is born"
    private final TokenRepository tokenRepository;
    
    
    //Save JWT to the Tokens table..
	//TODO i need these.. public is okay ?
	//TODO
    public void saveToken(User user, String tokenString) {
    	var token= Token.builder()
    			.user(user)
    			.token(tokenString)
    			.tokenType(TokenType.BEARER)
    			.expired(false)
    			.revoked(false)
    			.build();
    	tokenRepository.save(token);
    }
    
    //Function to revoke ALL user tokens 
    //why ? -> before login clear all previous tokens so user doesn't have many 
    //active sessions
    private void revokeAllTokens(User user) {
    	var validTokens=tokenRepository.findAllValidTokenByUser(user.getId());
    	if (validTokens.isEmpty()) return; //no active sessions 
    	
    	validTokens.forEach(token->{
    		token.setExpired(true);
    		token.setRevoked(true);
    	});
    	tokenRepository.saveAll(validTokens);
    }
    
    //Function1 -> Sign up 
    //1-Register = new user + token 
    public AuthenticationResponse register(RegisterRequest request) {// does not return authenticationResponse anymore..
    	//new object -> builder better anyways
    	//before register -> exists already ??
    	
    	if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("The Email: " + request.getEmail() + " already exists.");
        }
    	//else start building 
    	User user = User.builder()
    			.prenom(request.getPrenom())
    			.nom(request.getNom())
    			.email(request.getEmail())
    			//mdp -> encode again
    			.motDePasse(passwordEncoder.encode(request.getPassword()))
    			.role(UserRole.CUSTOMER)// basic  
    			.actif(true)
    			.build();
    	//crated -> save to repo 
    	var savedUser=userRepo.save(user);
    	
    	//NOPE NOT ANYMORE! -> register purely to save -> redirect to logn 
    	//NOW LOGIN WILL MAKE THE TOKENS + js intercepts !!
    	
    	
    	//saved -> tokenSSS for them! 
    	var token= jwtService.generateToken(user);
    	var refreshToken=jwtService.generateRefreshToken(user);
    	
    	//saveToken(savedUser, token);
    	
    	
    	return AuthenticationResponse.builder()
    			.accessToken(token)
    			.refreshToken(refreshToken)
    			.build();

    }
    
    //Function2 -> Log in
    //check email + encoded password if they match in DB -> give them token
    
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
    	//MANAGER -> checks email + password in the DB
    	authenticationManager.authenticate(
    			new UsernamePasswordAuthenticationToken(
    					//built in -> fills in with claims 
    					//IT HAS isAuthenticated = FALSE by default WOW
    					request.getEmail(),
    					request.getPassword())
    			);
    	
    	//now all good no exceptions -> FIND use -> GIVE THEM TOKEN!
    	var user = userRepo.findByEmail(request.getEmail()).orElseThrow(
    			() -> new RuntimeException("User not found"));
    	//TODO CAHNGE TO CUSTOM MESSAGE! !!
    			
    		
    	
    	
    	var token=jwtService.generateToken(user);
    	// + refresh token
    	var refreshToken = jwtService.generateRefreshToken(user);
    	
    	//CLEAR ALL SESSIONS IF ANY alrady from other devices 
    	revokeAllTokens(user);
    	//new tokens: 
    	saveToken(user,token);
    	
    	return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }
    
    //REFRESHING THE TOKEN now
    public void refreshToken(
    		HttpServletRequest request,
    		HttpServletResponse response //-> send new JSON
    		) throws IOException{
    	//why IO ? -> ObjectMapper -> write DIRECTLY to the output stream dang..
    	
    	final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        
        if (authHeader ==null|| !authHeader.startsWith("Bearer")){
        	return; 
        }
        
        //extract token + email from the existing token
        refreshToken=authHeader.substring(7);//remove "bearer" word
        userEmail=jwtService.extractUsername(refreshToken);
        
        //if email done ? ->
        if (userEmail!=null) {
        	//match the user by email in the DB
        	var user = userRepo.findByEmail(userEmail).orElseThrow(); //crash if doesn't exist lol
        	if (jwtService.isTokenValid(refreshToken, user)) {
        		//new ACCESS token the big one
        		var accessToken=jwtService.generateToken(user);
        		
        		//clear 
        		revokeAllTokens(user);
        		//save new 
        		saveToken(user,accessToken);
        		
        		//response = BOTH tokens
        		var authResponse= AuthenticationResponse.builder()
        				.accessToken(accessToken)
        				.refreshToken(refreshToken)
        				.build();
        		
        		//convert java object -> JSON string + write onto the HTTP response body(stream)
        		//new ObjectMapper().writeValue(response.getOutputStream(),authResponse);
        		
        		//postman test reutrning it
        		response.setContentType("application/json"); // Tell Postman it's JSON
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
        		
        	}else {
        		//refrehs token not valid -> cannot refresh it -> go back to login 
        		 throw new InvalidRefreshTokenException("the refresh Token is invalid/expired, please login again.");
        	}
        		
        }   
    }

	//new helper function, just to reissue tokens
	public AuthenticationResponse reissueTokens(User user){
		String accessToken = jwtService.generateToken(user);
		String refreshToken = jwtService.generateRefreshToken(user);

		revokeAllTokens(user);
		saveToken(user,accessToken);

		return AuthenticationResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
	}
    
      
    
}

