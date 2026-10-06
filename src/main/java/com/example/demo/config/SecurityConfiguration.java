package com.example.demo.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.auth.JwtAuthenticationFilter;
import com.example.demo.auth.LogoutService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
	//what it do ?
	//->SPRING  blocks EVERYTHING by default! 
	//-> that FilerChain -> create exceptions for login and authenticate
	//BUT eveyrhting else block!
	
	//DEPENDENCY INJECTION! -> CONSTRUCTOR injection!!!
	//final = MUST be initlizaed when creating the class, can never be replaced
	//-> lombok requiredArgsConstructor -> sees final -> generates constrcutor ! implicit!
	private final JwtAuthenticationFilter jwtAuthFilter; 
	private final AuthenticationProvider authProvider;
	private final LogoutService logoutService;
	
	@Bean
	//= a CHECKLIST! 
	//1-JWT -> so CSRF off
	//2-requestMatcher -> permitAll for register and login 
	//3-anyrequest .authenticated -> else aks for JWT 
	//4-session STATELESS -> = DO NOT remember anyone! EVERY time they come back -> ask for JWT always
	//makes it a REST API
	//6-addFilterBefore -> check ID before asking for username and password
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http
		.cors(Customizer.withDefaults())// -> use the @CrossOrigin in the contoller!
		.csrf(csrf->csrf.disable())//stateless API -> disable CSRF, cross site request forgery

		//THIS IS NEEDEDDD
				.exceptionHandling(exception -> exception
				.authenticationEntryPoint((request, response, authException) -> {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Force 401 Unauthorized!
					response.setContentType("application/json");
					response.getWriter().write("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Token expired or invalid\"}");
				})
		)

		.authorizeHttpRequests(auth->auth
				//1-REST API (AuthenticationController)
				// ---- CLEAN UP !!
				//-> Auth + swagger + H2 + error to keep
				.requestMatchers("/api/auth/**").permitAll()//allow ath apis
				.requestMatchers("/swagger-ui/**","/swagger-ui.html","/v3/api-docs/").permitAll()
				.requestMatchers("/h2-console/**").permitAll()
				.requestMatchers("/error").permitAll()




				.requestMatchers("/api/coupon/**").authenticated()
				.requestMatchers("/checkout").permitAll()
				.requestMatchers("/api/addresses/**").authenticated()
				
				.requestMatchers("/api/orders/checkout").authenticated()
					
				
				//2-THYMELEAF HTML
				.requestMatchers("/login", "/register","/cart").permitAll()
				.requestMatchers("/login", "/register", "/api/auth/**").permitAll()
				
				//SPRING HAS A /ERROR PAGE ??
				.requestMatchers("/error").permitAll()
				
				
				 .requestMatchers("/api/cart/**").permitAll()
				 .requestMatchers("/api/cart/checkout").permitAll()
				
				//TODO TEMPORARY FOR CATALOG TESTING + /shop filtering
				.requestMatchers("/catalog").permitAll()
				.requestMatchers("/shop").permitAll()
				.requestMatchers("/home").permitAll()
				.requestMatchers("/become-seller").permitAll()
				.requestMatchers("/post-product").permitAll()

				.requestMatchers("/api/dashboard/seller").permitAll()
				.requestMatchers("/api/dashboard/admin").permitAll()
				//(e.g., /swagger-ui/**, /v3/api-docs/**)
				

				
				.requestMatchers("/swagger-ui/**").permitAll()
				.requestMatchers("/swagger-ui.html").permitAll()
				.requestMatchers("/v3/api-docs/**").permitAll()
				
				.requestMatchers(HttpMethod.GET, "/api/products/**").authenticated() //
			    .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
			    
			    //TODO TEMPORARY
			    .requestMatchers("/product/**").permitAll()
			   
			    //.requestMatchers("/api/cart/**").hasRole("CUSTOMER")

				.requestMatchers("/actuator/**").permitAll()
				
				

				
				//4-CSS, JS, images (static)
				.requestMatchers("/css/**", "/js/**", "/img/**","/favicon.ico").permitAll() // the usual.. 
				
				
				
				.anyRequest().authenticated()//else need TOKEN!
				)
				.headers(headers -> headers.frameOptions(frame -> frame.disable()))//unblock H2 frame tags

		
		.authenticationProvider(authProvider) // OUR provider 
		.addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class )
		
		
		
		
		//Login -> when /logout use the LogoutService
		.logout(logout->
			logout.logoutUrl("/api/auth/logout")
			.addLogoutHandler(logoutService)
			.logoutSuccessHandler((request,response,authentification)->
			SecurityContextHolder.clearContext())
			//we already clear in the logoutService
			//still clear again -> Best practice!
			);
		
			return http.build();
	}
	
	//JS IT WONT WORK ! this should totally BYPASS JS
	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
	    return (web) -> web.ignoring()
	        .requestMatchers("/js/**", "/css/**", "/img/**", "/favicon.ico");
	}
	
}
