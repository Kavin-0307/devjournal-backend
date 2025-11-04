package com.example.demo.dto;
//it is to bundle data together.Whenever user logs in the server will create an instance of AuthResponseDTO
//it uses a record and it makes compiler make getter methods

public record AuthResponseDTO (
		String token,
		String username,
		String role
		) {}
  

