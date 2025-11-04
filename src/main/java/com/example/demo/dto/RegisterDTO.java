package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDTO {
	//it acts as Data Transfer Object and specifically to carry the data when new user registers
	@NotBlank
	@Size(max=90,message="User Name must be within 90 characters")
	private String username;
	@NotBlank(message="Password Required")
	private String password;
	public String getPassword()
	{
		return password;
	}
	public String getUsername()
	{
		return username;
	}
	public void setUsername(String username)
	{
		this.username=username;
	}
	public void setPassword(String password)
	{
		this.password=password;
	}
	

}
