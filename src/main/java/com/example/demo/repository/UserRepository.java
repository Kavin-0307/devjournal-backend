package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User,Long> {
	@Query("SELECT u FROM User u WHERE u.username = :username")
	Optional<User> findByUsername(String username);

}
