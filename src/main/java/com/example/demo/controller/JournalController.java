package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.JournalRequestDTO;
import com.example.demo.dto.JournalResponseDTO;
import com.example.demo.dto.PaginatedJournalResponseDTO;
import com.example.demo.service.CurrentUserService;
import com.example.demo.service.JournalService;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/entries")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class JournalController {

    Logger logger = LoggerFactory.getLogger(JournalController.class);

    @Autowired
    private CurrentUserService currentUserService;

    private final JournalService service;

    @Autowired
    public JournalController(JournalService service) {
        this.service = service;
    }

    @PostMapping("/journal")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<JournalResponseDTO> createJournalEntry(
            @Valid @RequestBody JournalRequestDTO requestDTO) {

        logger.info("Create new journal entry");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(requestDTO));
    }

    @PutMapping("/{id}/pin")
    public ResponseEntity<Void> togglePin(@PathVariable Long id) {
        service.togglePin(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/journal")
    public PaginatedJournalResponseDTO getAllJournalEntries(
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt,desc") String sort) {

        if (tags == null || tags.isEmpty()) {
            return service.findAllPaginated(page, size, sort);
        } else {
            return service.searchByTagsPaginated(tags, page, size, sort);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalResponseDTO> findJournalEntryById(@PathVariable("id") Long id) {
        return ResponseEntity.of(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JournalResponseDTO> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody JournalRequestDTO requestDTO) {

        return ResponseEntity.ok(service.update(requestDTO, id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<JournalResponseDTO>> searchEntries(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content) {

        return ResponseEntity.ok(service.search(title, content));
    }

    @GetMapping("/tags")
    public List<String> getUserTags() {
        String username = currentUserService.getCurrentUser().getUsername();
        return service.getTagsForUser(username);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> exportEntryToMarkdown(@PathVariable("id") Long id) {

        String markdown = service.exposeEntryToMarkdown(id);
        ByteArrayResource resource = new ByteArrayResource(markdown.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=entry_" + id + ".md");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(markdown.length())
                .contentType(MediaType.parseMediaType("text/markdown"))
                .body(resource);
    }

    @GetMapping("/export/all")
    public ResponseEntity<Resource> exportAllEntriesToZip() {

        byte[] zipBytes = service.exportAllEntriesToZip();
        ByteArrayResource resource = new ByteArrayResource(zipBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=journal_export.zip");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(zipBytes.length)
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable("id") Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
