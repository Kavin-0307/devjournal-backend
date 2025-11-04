package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.JournalEntry;
import com.example.demo.entity.Tag;

@Repository
//used as the actual database
public interface JournalRepository extends JpaRepository<JournalEntry,Long>{
	List<JournalEntry> findByEntryTitle(String entryTitle);
	Optional<JournalEntry> findByIdAndUser_Id(Long id,Long userId);
	List<JournalEntry> findAllByUser_Id(Long userId);
	List<JournalEntry> findAllByUser_IdAndEntryTitleContainingIgnoreCase(Long userId, String title);
	List<JournalEntry> findAllByUser_IdAndContentContainingIgnoreCase(Long userId, String content);
	List<JournalEntry> findAllByUser_IdAndEntryTitleContainingIgnoreCaseOrContentContainingIgnoreCase(Long userId, String title, String content);
	List<JournalEntry> findAllByUser_IdOrderByUpdatedAtDesc(Long userId);
	Page<JournalEntry> findAllByUser_Id(Long userId,Pageable pageable);
	@Query("""
		    SELECT e FROM JournalEntry e
		    JOIN e.tags et
		    JOIN et.tag t
		    WHERE e.user.id = :userId
		      AND t.name IN :tags
		    GROUP BY e.id
		    HAVING COUNT(DISTINCT t.name) = :tagCount
		""")
		Page<JournalEntry> searchByUserAndTags(Long userId, List<String> tags, long tagCount,Pageable pageable);
	@Query("""
		    SELECT e FROM JournalEntry e
		    JOIN e.tags et
		    JOIN et.tag t
		    WHERE e.user.id = :userId
		      AND t.name IN :tags
		    GROUP BY e.id
		    HAVING COUNT(DISTINCT t.name) = :tagCount
		""")
		List<JournalEntry> searchByUserAndTags(Long userId, List<String> tags, long tagCount);
		
}
