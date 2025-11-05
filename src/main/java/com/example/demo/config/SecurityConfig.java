package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.filter.JwtAuthenticationFilter;
import com.example.demo.service.UserDetailsServiceImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
//used to tell that the class defines the web security rules.The class sets up a stateless and token based authentication system using JWT
public class SecurityConfig {
	
	@Autowired
	private JwtAuthenticationFilter jwtAuthFilter;//handles incoming request and checks JWT in Authorization header
	@Autowired 
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;//used to send error if user tries to acces endpoint which is protected
	//sets dependencies properly 
	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint){
this.jwtAuthFilter = jwtAuthFilter;
this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
}
	
	@Bean//used by Spring to create objects that is used to build its config
	public PasswordEncoder passwordEncoder()//return bcryptpasswordencoder to help us encode
	{
		return new BCryptPasswordEncoder();
	}
	@Bean
	public DaoAuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsServiceImpl)//checks whether username and password valid
	{
		DaoAuthenticationProvider provider =new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsServiceImpl);//uses userDetailsServiceImpl to find a user by username
		provider.setPasswordEncoder(passwordEncoder());//use password encoder to check if users password matches the hashed password in db
		return provider;
	}
	@Bean
	//creates AuthManager to process the login request 
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
	    return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
	                                               DaoAuthenticationProvider authenticationProvider) throws Exception {

	    http
	        .csrf(AbstractHttpConfigurer::disable)
	        .cors(cors -> cors.configurationSource(request -> {
	            var config = new org.springframework.web.cors.CorsConfiguration();
	            config.setAllowCredentials(true);
	            config.addAllowedOrigin("http://localhost:5173"); // LOCAL frontend
	            config.addAllowedOrigin("https://devjournal-frontend-production-url-if-you-deploy"); // Future deploy
	            config.addAllowedHeader("*");
	            config.addAllowedMethod("*");
	            return config;
	        }))
	        .authorizeHttpRequests(auth -> auth
	                .requestMatchers("/api/auth/**").permitAll()  // login/register allowed
	                .requestMatchers("/entries/**").authenticated() // protect journal APIs
	                .anyRequest().authenticated()
	        )
	        .sessionManagement(session -> session
	                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	        )
	        .authenticationProvider(authenticationProvider)
	        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
	        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

	    return http.build();
	}


}
