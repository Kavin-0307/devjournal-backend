package com.example.demo.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;

public class UserDetailsImpl implements UserDetails,CredentialsContainer{
	private String username;
	private String password;
	private String role;
	private boolean enabled;
	public UserDetailsImpl(User user)
	{
		this.username=user.getUsername();
		this.password=user.getPassword();
		this.role=user.getRole();
		enabled=user.isEnabled();
	}
	public String getUsername()
	{
		return username;
	}
	public String getPassword()
	{
		return password;
	}
	
	public boolean isAccountNonExpired()
	{
		return true;
	}
	public boolean isCredentialsNonExpired()
	{
		return true;
	}
	public boolean isEnabled()
	{
		return enabled;
	}
	@Override
	public void eraseCredentials() {
		// TODO Auto-generated method stub
		this.password=null;
	}
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		return List.of(new SimpleGrantedAuthority(role));
	}
	@Override
	public boolean isAccountNonLocked() {
	    return true;
	}

}
