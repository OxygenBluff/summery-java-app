package com.example.demo.auth;

import java.io.IOException;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.repositories.TokenRepository;
import com.example.demo.security.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
//and what the helly this do ?
//->INTERCEPTS !!! every API request!!! 
//->checks if header has "Bearer" 
//-> asks the JWT Service is token is valid 
//-> if yes allows!
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;
	
	private final TokenRepository tokenRepo;
	
	@Override
	//the guard! is this doFilterInternal
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
			) throws ServletException, IOException{
		//request can never be null -> else crash with message
		
		final String authHeader = request.getHeader("Authorization");
        final String jwt;
        //final String userEmail; //cahged
		String userEmail = null;

		//HMM ? if auth just don't check token
		if(request.getServletPath().contains("/api/auth")){
			filterChain.doFilter(request,response);
			return;
		}

        //if no bearer token -> move to next filter 
        
        if(authHeader==null || !authHeader.startsWith("Bearer")) {
        	filterChain.doFilter(request, response);
        	return;
        }
        
        //EXTRACT TOKEN -> need to remove "Bearer" 7 letters
        jwt = authHeader.substring(7).trim();

		//NO JUST PASS -> flag as 401 not 403 !!!
        try {
        userEmail= jwtService.extractUsername(jwt);

        }catch(ExpiredJwtException e) {
			//TOOD FIX ? -> not write ANTYHING early just leaving userEmail or SecurityContet EMPTY ???

			/*
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // =401
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"Token expired\"}");
			return; // TODO NO LONGER CONTINUING WITH THE FITLER CHAIN WITH 401

			//TODO BUT THAT BREAKS SPRING CORS STUFF WHATTTTT -> get 403 instead
			 */
			request.setAttribute("expired", e.getMessage());

			//why the \ ?
			//-> in java cannot use " isnide " !! escape inner one using \ before any new one
        }catch(Exception e) {
			//same thing here
			/*
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"Invalid token\"}");
			return;

			 */
            //any other issues
			request.setAttribute("invalidToken", e.getMessage());
        }
        
        //1-you have bearer token ? -> if no move to the next guard
        //2->is token valid ? (JwtService) 
        //3-NOW ur good -> put YOUR NAME in the SecutyConext ->
        //Controller knows ur allowed!
        
        //CASE: Email + user not authenticated yet -> for register and login!
        //SecurityContextHolder.getContext().getAuthentication() == null)
        //null meail = FAKE! -> just stop


        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            //-> i have an email form a JWT -> go to the Db -> find the ROW for this email 
            //-> turn it inot a JAVA OBJECT !
            //returns just user cuz User implemets!UserDeatail
            
            //!!! with token table + refresh token ->
        	//get token from DB now 
            
            var isTokenValid = tokenRepo.findByToken(jwt)
            		.map(token->!token.isExpired()&& !token.isRevoked())
            		.orElse(false); //java became python ABSOLUTE CINEMA .map
            //-> check token exists + Not revoked or expired
            
            
            //VALIDER LE TOKEN DB: 
            //-> why ? User might have changed their toles or not active..
            
            if(jwtService.isTokenValid(jwt, userDetails) && isTokenValid) {
            	UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            			userDetails,
            			null, //PASSWORD IS NULL no need! just JWT we're stateless!! (cerdentials)
            			userDetails.getAuthorities()
            		);
            	
            	//ADD DETAILS TO TOKEN: IP, session, -> from the request
            	authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            	
            	//UPDATE SECURITY CONTEXT:
            	SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        //CALL NEXT FILTER  ->
        filterChain.doFilter(request,response);
	}
}


