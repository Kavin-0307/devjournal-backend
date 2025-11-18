package com.example.demo.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

	        // ✅ AUTH RULES
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/api/auth/**").permitAll()
	            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
	            .anyRequest().authenticated()
	        )

	        // ✅ CORS FIX
	        .cors(cors -> cors.configurationSource(request -> {
	            CorsConfiguration config = new CorsConfiguration();
	            config.setAllowCredentials(true);
	            config.setAllowedOriginPatterns(List.of(
	                "http://localhost:5173",
	                "http://127.0.0.1:5173",
	                "https://*.up.railway.app",
	                "https://devjournal-frontend.vercel.app"
	            ));
	            config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
	            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
	            config.setExposedHeaders(List.of("Authorization"));
	            return config;
	        }))

	        // ✅ REGISTER AUTH PROVIDER
	        .authenticationProvider(authenticationProvider)

	        // ✅ ADD JWT FILTER BEFORE SPRING AUTHENTICATION FILTER
	        .addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);
	    return http.build();

	}

	@Bean
	public CorsFilter corsFilter() {
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true);
	    config.setAllowedOriginPatterns(List.of(
    "http://localhost:5173",
    "http://127.0.0.1:5173",
    "https://*.up.railway.app",
    "https://devjournal-frontend.vercel.app"
));

	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
	    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Origin", "Accept"));
	    config.setExposedHeaders(List.of("Authorization"));

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}

}
