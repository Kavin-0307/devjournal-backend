package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
@Service
public class CurrentUserService {
	@Autowired
	private UserRepository userRepository;
	
	public User getCurrentUser()
	{
		String username=SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not found"));
		
	
	}
	public Long getCurrentUserId()
	{
		return getCurrentUser().getId();
	}
}
