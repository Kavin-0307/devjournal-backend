package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequestDTO {
//created to carry the user's credentials specifically during login request.It is the expected structure for JSON payload sent from the client to endpoint
	@NotBlank
	@Size(max=90,message="User Name must be within 90 character")
	private String username;
	@NotBlank(message="Password reqd")
	private String password;
	public void setUsername(String username)
	{
		this.username=username;
	}
	public void setPassword(String password)
	{
		this.password=password;
	}
	public String getUsername()
	{
		return username;
	}
	public String getPassword()
	{
		return password;
	}
}
