package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.AuthRequestDTO;
import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
@Service
public class UserService {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	public UserService(UserRepository userRepository,JwtService jwtService)
	{
		this.jwtService=jwtService;
		this.userRepository=userRepository;
	}
	public void registerUser(RegisterDTO registerDTO)
	{
		if(userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
			throw new IllegalArgumentException("there is already such a username");
		}
		User user=new User();
		user.setUsername(registerDTO.getUsername());
		user.setRole("USER");
		user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
		userRepository.save(user);
	}
	public AuthResponseDTO authenticateUser(AuthRequestDTO requestDTO)
	{
		User user=userRepository.findByUsername(requestDTO.getUsername()).orElseThrow(()->new IllegalArgumentException
				("Invalid Username or Password"));
		if(!passwordEncoder.matches(requestDTO.getPassword(),user.getPassword()))
			throw new IllegalArgumentException("Invalid username or password");	
		String token = jwtService.generateToken(user.getUsername(), user.getRole());
		return new AuthResponseDTO(token, user.getUsername(), user.getRole());

	}

}
