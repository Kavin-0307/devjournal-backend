package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserDetailsImpl;
@Service
public class UserDetailsServiceImpl implements UserDetailsService{
	
	private final UserRepository userRepository;
	public UserDetailsServiceImpl(UserRepository userRepository)
	{
		this.userRepository=userRepository;
	}
	
	public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException
	{
		User user = userRepository.findByUsername(username)
		        .orElseThrow(() -> new UsernameNotFoundException("No user exists with this name"));

		
		return new UserDetailsImpl(user);

		
	}
}
