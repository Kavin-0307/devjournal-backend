package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.service.JwtService;
import com.example.demo.service.UserDetailsServiceImpl;
import com.example.demo.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")


public class AuthController {
	
	private UserService userService;
	private final JwtService jwtService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired 
	public AuthController(UserService userService,JwtService jwtService)
	{
		this.userService=userService;
		this.jwtService=jwtService;
	}
	
	
	@PostMapping("register")
	public ResponseEntity<String> register(@Valid@RequestBody RegisterDTO registerDTO)
	{
		//accepts RegisterDTO from the user request body
		//vali triggers validation checcks on registerDTO fields and delegates user creation to userservice .registerUser .It is responsible for hashing the password and save the new User entity
		userService.registerUser(registerDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
	}
	@PostMapping("login")
	public ResponseEntity<AuthResponseDTO> handleLogin(@Valid@RequestBody AuthRequestDTO authRequestDTO)
	{
		//accepts an authrequestDTO it uses  spring security authmanager to  verify credentials
		AuthResponseDTO response=userService.authenticateUser(authRequestDTO);
		return ResponseEntity.ok(response);
	}
}
