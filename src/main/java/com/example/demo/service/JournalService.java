package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;

import com.example.demo.dto.JournalRequestDTO;
import com.example.demo.dto.JournalResponseDTO;
import com.example.demo.dto.PaginatedJournalResponseDTO;
import com.example.demo.entity.EntryTag;
import com.example.demo.entity.JournalEntry;
import com.example.demo.entity.Tag;
import com.example.demo.entity.User;
import com.example.demo.repository.JournalRepository;
import com.example.demo.repository.TagRepository;

@Service
public class JournalService {

    private final JournalRepository journalRepository;
    private final TagRepository tagRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    public JournalService(JournalRepository journalRepository, TagRepository tagRepository) {
        this.journalRepository = journalRepository;
        this.tagRepository = tagRepository;
    }

    public JournalResponseDTO create(JournalRequestDTO requestDTO) {

        JournalEntry entry = convertToEntity(requestDTO);
        entry.setUser(currentUserService.getCurrentUser());
        JournalEntry savedEntry = journalRepository.save(entry);

        if (requestDTO.getTags() != null) {
            User currentUser = currentUserService.getCurrentUser();
            for (String tagName : requestDTO.getTags()) {
                String clean = tagName.toLowerCase().trim();
                Tag tag = tagRepository.findByNameAndUser(clean, currentUser)
                        .orElseGet(() -> tagRepository.save(new Tag(clean, currentUser)));
                savedEntry.getTags().add(new EntryTag(savedEntry, tag));
            }
        }

        savedEntry = journalRepository.save(savedEntry);
        return convertToResponseDTO(savedEntry);
    }

    public JournalResponseDTO update(JournalRequestDTO requestDTO, Long id) {

        JournalEntry entry = journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found or unauthorized"));

        entry.setEntryTitle(requestDTO.getEntryTitle());
        entry.setContent(requestDTO.getContent());

        entry.getTags().clear();
        journalRepository.save(entry);

        if (requestDTO.getTags() != null) {
            User currentUser = currentUserService.getCurrentUser();
            for (String tagName : requestDTO.getTags()) {
                String clean = tagName.toLowerCase().trim();
                Tag tag = tagRepository.findByNameAndUser(clean, currentUser)
                        .orElseGet(() -> tagRepository.save(new Tag(clean, currentUser)));
                entry.getTags().add(new EntryTag(entry, tag));
            }
        }

        JournalEntry updated = journalRepository.save(entry);
        return convertToResponseDTO(updated);
    }

    public List<JournalResponseDTO> findAll() {
        return journalRepository.findAllByUser_IdOrderByUpdatedAtDesc(currentUserService.getCurrentUserId())
                .stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    public PaginatedJournalResponseDTO findAllPaginated(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String sortField = parts[0];
        Sort.Direction direction = (parts.length > 1 && parts[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Long userId = currentUserService.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<JournalEntry> journalPage = journalRepository.findAllByUser_Id(userId, pageRequest);
        List<JournalResponseDTO> entries = journalPage.getContent().stream().map(this::convertToResponseDTO).collect(Collectors.toList());
        return new PaginatedJournalResponseDTO(entries, journalPage.getNumber(), journalPage.getSize(),
                journalPage.getTotalPages(), journalPage.getTotalElements());
    }

    public Optional<JournalResponseDTO> findById(Long id) {
        return journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
                .map(this::convertToResponseDTO);
    }

    public void deleteById(Long id) {
        journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("You are not allowed to delete this entry or it does not exist."));
        journalRepository.deleteById(id);
    }

    public String exposeEntryToMarkdown(Long entryId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        JournalEntry entry = journalRepository.findByIdAndUser_Id(entryId, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("No user exists with this name"));
        return "#"+entry.getEntryTitle()+"\n\n"
                + entry.getContent()+"\n\n"
                + "Tags:" + entry.getTags().stream().map(e -> e.getTag().getName()).collect(Collectors.joining(", ")) + "\n\n"
                + "Created:" + entry.getCreationDate()+"\n\n";
    }

    public void togglePin(Long id) {
        JournalEntry entry = journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Entry not found"));
        entry.setPinned(!entry.isPinned());
        journalRepository.save(entry);
    }

    public JournalEntry convertToEntity(JournalRequestDTO dto) {
        JournalEntry entry = new JournalEntry();
        entry.setEntryTitle(dto.getEntryTitle());
        entry.setContent(dto.getContent());
        return entry;
    }

    public JournalResponseDTO convertToResponseDTO(JournalEntry entry) {
        return new JournalResponseDTO(
                entry.getId(),
                entry.getEntryTitle(),
                entry.getContent(),
                entry.getCreationDate(),
                entry.getTags().stream().map(e -> e.getTag().getName()).collect(Collectors.toList()),
                entry.getUpdatedAt(),
                entry.isPinned()
        );
    }

    public List<String> getTagsForUser(String username) {
        return tagRepository.findDistinctTagNamesByUsername(username);
    }
}
