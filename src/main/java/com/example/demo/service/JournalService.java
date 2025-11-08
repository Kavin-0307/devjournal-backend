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

    // CREATE
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

        return convertToResponseDTO(journalRepository.save(savedEntry));
    }

    // UPDATE
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

        return convertToResponseDTO(journalRepository.save(entry));
    }

    // GET ALL
    public List<JournalResponseDTO> findAll() {
        return journalRepository.findAllByUser_IdOrderByUpdatedAtDesc(currentUserService.getCurrentUserId())
                .stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    // PAGINATED LIST
    public PaginatedJournalResponseDTO findAllPaginated(int page, int size, String sort) {
        String[] parts = sort.split(",");
        Long userId = currentUserService.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(
                (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC,
                parts[0]));
        Page<JournalEntry> journalPage = journalRepository.findAllByUser_Id(userId, pageRequest);

        return new PaginatedJournalResponseDTO(
                journalPage.getContent().stream().map(this::convertToResponseDTO).collect(Collectors.toList()),
                journalPage.getNumber(), journalPage.getSize(),
                journalPage.getTotalPages(), journalPage.getTotalElements());
    }

    // FIND BY ID
    public Optional<JournalResponseDTO> findById(Long id) {
        return journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
                .map(this::convertToResponseDTO);
    }

    // DELETE
    public void deleteById(Long id) {
        journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Not allowed"));
        journalRepository.deleteById(id);
    }

    // SEARCH
    public List<JournalResponseDTO> search(String title, String content) {
        Long userId = currentUserService.getCurrentUserId();
        if (title != null && content != null)
            return journalRepository.findAllByUser_IdAndEntryTitleContainingIgnoreCaseOrContentContainingIgnoreCase(userId, title, content).stream().map(this::convertToResponseDTO).toList();
        if (title != null)
            return journalRepository.findAllByUser_IdAndEntryTitleContainingIgnoreCase(userId, title).stream().map(this::convertToResponseDTO).toList();
        return journalRepository.findAllByUser_IdAndContentContainingIgnoreCase(userId, content).stream().map(this::convertToResponseDTO).toList();
    }

    // SEARCH BY TAGS (LIST)
    public List<JournalResponseDTO> searchByTags(List<String> tags) {
        List<String> normalized = tags.stream().map(tag -> tag.toLowerCase().trim()).toList();
        Long userId = currentUserService.getCurrentUserId();
        return journalRepository.searchByUserAndTags(userId, normalized, normalized.size()).stream().map(this::convertToResponseDTO).toList();
    }

    // SEARCH BY TAGS (PAGINATED)
    public PaginatedJournalResponseDTO searchByTagsPaginated(List<String> tags, int page, int size, String sort) {
        List<String> normalized = tags.stream().map(tag -> tag.toLowerCase().trim()).toList();
        Long userId = currentUserService.getCurrentUserId();
        String[] parts = sort.split(",");
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(
                (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC,
                parts[0]));
        Page<JournalEntry> journalPage = journalRepository.searchByUserAndTags(userId, normalized, normalized.size(), pageRequest);

        return new PaginatedJournalResponseDTO(
                journalPage.getContent().stream().map(this::convertToResponseDTO).collect(Collectors.toList()),
                journalPage.getNumber(), journalPage.getSize(), journalPage.getTotalPages(), journalPage.getTotalElements());
    }

    // EXPORT ZIP
    public byte[] exportAllEntriesToZip() {
        Long userId = currentUserService.getCurrentUserId();
        List<JournalEntry> allEntries = journalRepository.findAllByUser_Id(userId);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(output)) {
            for (JournalEntry entry : allEntries) {
                String markdown = exposeEntryToMarkdown(entry.getId());
                zipOut.putNextEntry(new ZipEntry("entry_" + entry.getId() + ".md"));
                zipOut.write(markdown.getBytes());
                zipOut.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException("ZIP export failed", e);
        }

        return output.toByteArray();
    }

    // MARKDOWN EXPORT HELPER
    public String exposeEntryToMarkdown(Long entryId) {
        JournalEntry entry = journalRepository.findByIdAndUser_Id(entryId, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Not allowed"));
        return "#" + entry.getEntryTitle() + "\n\n"
                + entry.getContent() + "\n\n"
                + "Tags:" + entry.getTags().stream().map(e -> e.getTag().getName()).collect(Collectors.joining(", ")) + "\n\n"
                + "Created:" + entry.getCreationDate() + "\n\n";
    }

    // PIN
    public void togglePin(Long id) {
        JournalEntry entry = journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Not allowed"));
        entry.setPinned(!entry.isPinned());
        journalRepository.save(entry);
    }

    // DTO HELPERS
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
                entry.isPinned());
    }

    public List<String> getTagsForUser(String username) {
        return tagRepository.findDistinctTagNamesByUsername(username);
    }
}
