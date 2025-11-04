package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Tag;

public interface TagRepository extends JpaRepository<Tag,Long> {
	Optional<Tag> findByName(String name);
	@Query("SELECT t.name FROM Tag t ORDER BY LOWER(t.name) ")

	List<String> findAllTagNames();
}
