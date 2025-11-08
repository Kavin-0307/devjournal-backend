package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.Tag;
import com.example.demo.entity.User;

public interface TagRepository extends JpaRepository<Tag,Long> {
	Optional<Tag> findByName(String name);
	@Query("""
		       SELECT DISTINCT t.name
		       FROM EntryTag et
		       JOIN et.tag t
		       JOIN et.journalEntry je
		       WHERE je.user.username = :username
		       ORDER BY LOWER(t.name)
		       """)
		List<String> findDistinctTagNamesByUsername(String username);
	Optional<Tag> findByNameAndUser(String name, User user);

}
