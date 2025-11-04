package com.example.demo.dto;

import java.util.List;

public record PaginatedJournalResponseDTO (
		List<JournalResponseDTO> entries,
	int pageNumber,
	int entriesPerPage,
	int totalPages,
	long totalEntries
){}
