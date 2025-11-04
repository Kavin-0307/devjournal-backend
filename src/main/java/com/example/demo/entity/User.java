package com.example.demo.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name="Users")
public class User {
	//It is JPA entity and the model is  a user account in database.It workds with spring data Jpa
	@Id//marks id as primary key and configures to be auto incremented
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	
	@Column(name="Id")
	private Long id;
	@NotBlank
	@Column(name="user_name",length=90)
	private String username;	
	@Size(min=9)
	@Column(name="password",length=60)//length required for BCrypt Hash
	private String password;
	@Column(name="Role")
	private String role;
	@Column(name="CreatedAt")
	@CreationTimestamp
	private  LocalDateTime createdAt;
	
	private boolean enabled=true;
	public User(){
		
	}
	public User(String username)
	{
		this.username=username;
	}
	
	public void setUsername(String username)
	{
		this.username=username;
	}
	public String getUsername()
	{
		return username;
	}
	public void setPassword(String password)
	{
		this.password=password;
	}
	public String getPassword()
	{
		return password;
	}public void setCreatedAt(LocalDateTime createdAt)
	{
		this.createdAt=createdAt;
	}
	public LocalDateTime getCreatedAt()
	{
		return createdAt;
	}public void setRole(String role)
	{
		this.role=role;
	}
	public String getRole()
	{
		return role;
	}
	public boolean isEnabled() {
	    return enabled;
	}

	public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	}
	public void setId(long id)
	{
		this.id=id;
	}
	public Long getId()
	{
		return id;
	}

	
}
