package com.example.demo.dto;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
//used to create responseDTO
public record JournalResponseDTO (
	Long id,
	String entryTitle,
	String content,
	LocalDateTime creationDate,
	List<String> tags,
	 Date updatedAt,
	 boolean isPinned
) {}
