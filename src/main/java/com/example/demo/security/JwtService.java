package com.example.demo.security;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;


@Service
public class JwtService {
	//@Value -> get value from appl properties
	@Value("${application.security.jwt.secret-key}")
    private String secretKey;
	
	//access token, 15 mins ?
	private final long jwtExpiration = 30*60*1000; //ms
	//user uses this to browse ! 30*60*1000
			
	//refresh token , 7days ? 
	private final long refreshExpiration = 1000*60*60*24*14;  //  1000*60*60*24*14;
	//don't have to type password EVERY time you open app 
	
	//TOKEN = long string
	//has this structure: !!! header.payload.signature !!! 
	//payload = where the DATA is!
	//"CLAIMS" = data fields inside that payload
	//Claims=  key vals !! 
	
	
	
	private String buildToken(Map<String,Object> extraClaims, UserDetails userDetails, long expiration) {
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.claims(extraClaims)
				.subject(userDetails.getUsername())
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis()+expiration))
				.signWith(getSignInKey()) //IMPORTANT! -> locks the Claims(fields) with the SECRET KEY!!
				.compact(); //-> turns all data into a STRING
		
		//what is builder() -> IF OBKECT IS TOO COMPLEX
		//-> use builder instead of new Token() that's it! 
		}
	

	public String generateToken(UserDetails userDetails) {
		//hmm MY JWT isn't exposing the ROLE!!
		Map<String,Object> extraClaims = new HashMap<>();

		List<String> roles = userDetails.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)//how am i supposed to learn these functions GrantedAuth ?
				.toList();

		extraClaims.put("roles",roles);
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}
	
	//username from token, extract
		public String extractUsername(String token) {
	        return extractClaim(token, Claims::getSubject);
	        //Claims::getSubject = kinda remote control for a function
	        //-> when parsing token done and CLIAMS object is ready 
	        //->push getSubject into it ! :: = use the getSubject if that class
	    }
		
		//extracting CLAIMS = 
		public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
			//function <Claims,T> ? ->claims = input (MAP OF ALL DATA!)
			//T= OUPUT of that SPECIFIC piece of data Ex: String -> output of Date of expiration..
			//Ex: Claims::getSubject -> want SUBJECT field -> will return a String!
	        final Claims claims = extractAllClaims(token);
	        return claimsResolver.apply(claims);
	        //claimsResolver.apply -> GIVE the list of data fileds
	        //i'll pick what i want
	    }
	
	
	//generer -> REFRESH token
	public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }
	
	//token expired ?
	private boolean isTokenExpired(String token) {
		Date expiration =  extractClaim(token,Claims::getExpiration); // Claims::getExp -> POINTS to the getExpiration method 
		//IN THE CLAIMS interface!
		return expiration.before(new Date()); //expiration claim! date lt current one!
	}
	
	//toekn valide ? = valides + BELONGS TO THE USER!
	public boolean isTokenValid(String token, UserDetails userDet) {
		final String username=extractUsername(token);
		boolean is_valid=
				username.equals(userDet.getUsername()) && !isTokenExpired(token);
		
		return is_valid;

	}
	
	//EKY -> crypt SecretKey 
	private SecretKey getSignInKey() {
		//byte[] keyBytes=Decoders.BASE64.decode(secretKey);
		byte[] keyBytes=secretKey.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes); // this one needs a keyBytes array 
		//so specific.. 
		
		
	}
	//helper -> getAllClaims 
	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSignInKey())//passing the crypotgraphed key!
				.build()
				.parseSignedClaims(token)
				//parse signed claims ->LOOK at signature -> use
				//SECRET KEY to see if legit
				.getPayload();
	}

}
