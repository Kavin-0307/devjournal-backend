package com.example.demo.service;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
	public JournalService(JournalRepository journalRepository,TagRepository tagRepository) {
		this.journalRepository=journalRepository;
		this.tagRepository=tagRepository;
	}
	public JournalResponseDTO create(JournalRequestDTO requestDTO) {
		System.out.println("📌 Incoming tags = " + requestDTO.getTags());

	    JournalEntry entry = convertToEntity(requestDTO);
	    entry.setUser(currentUserService.getCurrentUser());

	    // Save FIRST to generate ID
	    JournalEntry savedEntry = journalRepository.save(entry);

	    // ✅ Clear old tags (safety for update flow reuse)
	   

	    if (requestDTO.getTags() != null) {
	        for (String tagName : requestDTO.getTags()) {

	            String clean = tagName.toLowerCase().trim();

	            Tag tag = tagRepository.findByName(clean)
	                    .orElseGet(() -> tagRepository.save(new Tag(clean)));

	            // ✅ Create join entity
	            EntryTag entryTag = new EntryTag(savedEntry, tag);

	            savedEntry.getTags().add(entryTag);
	        }
	    }

	    // ✅ Save again to store entry_tags
	    savedEntry = journalRepository.save(savedEntry);

	    return convertToResponseDTO(savedEntry);
	}


	public List<JournalResponseDTO> findAll()//it finds all entries of users with usernames.Then it converts to a list.It loops over everyJournalEntry and converts it into ResponseDTO using helper method
	{
		return journalRepository.findAllByUser_IdOrderByUpdatedAtDesc(currentUserService.getCurrentUserId()).stream().map(this::convertToResponseDTO).collect(Collectors.toList());
	}
	
	public PaginatedJournalResponseDTO findAllPaginated(int page,int size,String sort)
	{
		String [] parts=sort.split(",");
		String sortField=parts[0];
		Sort.Direction direction=(parts.length>1&&parts[1].equalsIgnoreCase("asc"))?Sort.Direction.ASC:Sort.Direction.DESC;
		Long userId=currentUserService.getCurrentUserId();
		PageRequest pageRequest=PageRequest.of(page,size,Sort.by(direction,sortField));
		Page<JournalEntry> journalPage=journalRepository.findAllByUser_Id(userId,pageRequest);
		List<JournalResponseDTO> entries=journalPage.getContent().stream().map(this::convertToResponseDTO).collect(Collectors.toList());
		int pageNumber=journalPage.getNumber();
		int entriesPerPage=journalPage.getSize();
		int totalPages=journalPage.getTotalPages();
		long totalEntries=journalPage.getTotalElements();
		return new PaginatedJournalResponseDTO(entries,pageNumber,entriesPerPage,totalPages,totalEntries);
	
	}
	public Optional<JournalResponseDTO> findById(Long id)
	{
		return journalRepository.findByIdAndUser_Id(id,currentUserService.getCurrentUserId()).map(this::convertToResponseDTO);//finds a single entry by id
	}
	public void deleteById(Long id)
	{
		 Optional<JournalEntry> entry = journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId());
		    if (entry.isEmpty()) {
		        throw new IllegalArgumentException("You are not allowed to delete this entry or it does not exist.");
		    }
		    journalRepository.delete(entry.get());
		
	}
	public byte[] exportAllEntriesToZip() {
	    Long userId = currentUserService.getCurrentUserId();
	    List<JournalEntry> allEntries = journalRepository.findAllByUser_Id(userId);

	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    try (ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {

	        for (JournalEntry entry : allEntries) {
	            String safeTitle = entry.getEntryTitle()
	                    .toLowerCase()
	                    .trim()
	                    .replaceAll("[^a-zA-Z0-9-_ ]", "")
	                    .replace(" ", "_");

	            String fileName = "entry_" + entry.getId() + "_" + safeTitle + ".md";

	            String markdown = this.exposeEntryToMarkdown(entry.getId());
	            ZipEntry zipEntry = new ZipEntry(fileName);

	            zipOut.putNextEntry(zipEntry);
	            zipOut.write(markdown.getBytes());
	            zipOut.closeEntry();
	        }

	    } catch (Exception e) {
	        throw new RuntimeException("Error creating ZIP export", e);
	    }

	    return byteArrayOutputStream.toByteArray();
	}

	public JournalResponseDTO update(JournalRequestDTO requestDTO, Long id) {
		System.out.println("📌 Incoming tags = " + requestDTO.getTags());

	    JournalEntry entry = journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
	            .orElseThrow(() -> new IllegalArgumentException("Journal entry not found or unauthorized"));

	    entry.setEntryTitle(requestDTO.getEntryTitle());
	    entry.setContent(requestDTO.getContent());

	    // 🚨 Clear old tag links
	    entry.getTags().clear();
	    journalRepository.save(entry); // flush clear to DB

	    if (requestDTO.getTags() != null) {
	        for (String tagName : requestDTO.getTags()) {
	            String clean = tagName.toLowerCase().trim();
	            Tag tag = tagRepository.findByName(clean)
	                    .orElseGet(() -> tagRepository.save(new Tag(clean)));

	            EntryTag entryTag = new EntryTag(entry, tag);
	            entry.getTags().add(entryTag);
	        }
	    }

	    JournalEntry updated = journalRepository.save(entry);
	    return convertToResponseDTO(updated);
	}
	
	
	public List<JournalResponseDTO> search(String title,String content)
	{
		if (title != null && title.trim().isEmpty()) title = null;
	    if (content != null && content.trim().isEmpty()) content = null;
	    if(title == null && content == null)
		    throw new IllegalArgumentException("Please provide at least one search value (title or content).");

		List<JournalEntry> results=null;
		Long userId=currentUserService.getCurrentUserId();
		if(title==null&&content!=null)
			results=journalRepository.findAllByUser_IdAndContentContainingIgnoreCase( userId, content);
		else if(title!=null&&content!=null)
			results=journalRepository.findAllByUser_IdAndEntryTitleContainingIgnoreCaseOrContentContainingIgnoreCase( userId,title, content);
		else if(title!=null&&content==null)
			results=journalRepository.findAllByUser_IdAndEntryTitleContainingIgnoreCase( userId,title);
		
		return  results.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
	
	}
	
	public List<JournalResponseDTO> searchByTags(List<String> tags)
	{
		
		List<String> normalizedTags = tags.stream()
			    .map(tag -> tag.toLowerCase().trim())
			    .toList();

		Long userId=currentUserService.getCurrentUserId();
		List<JournalEntry> entries=journalRepository.searchByUserAndTags(userId,normalizedTags,normalizedTags.size());
		List<JournalResponseDTO> result=entries.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
		return result;
	}
	
	
	public JournalEntry convertToEntity(JournalRequestDTO dto)
	{
		//helper method to allow interconversion of entity and ResponseDTp
		JournalEntry entry=new JournalEntry();
		entry.setEntryTitle(dto.getEntryTitle());
		entry.setContent(dto.getContent());
		return entry;
	}
	public PaginatedJournalResponseDTO searchByTagsPaginated(List<String> tags, int  page,int size, String sort)
	{

		List<String> normalizedTags = tags.stream()
			    .map(tag -> tag.toLowerCase().trim())
			    .toList();
		Long userId=currentUserService.getCurrentUserId();
		String [] parts=sort.split(",");
		String sortField=parts[0];
		Sort.Direction direction=(parts.length>1&&parts[1].equalsIgnoreCase("asc"))?Sort.Direction.ASC:Sort.Direction.DESC;
		
		PageRequest pageRequest=PageRequest.of(page,size,Sort.by(direction,sortField));
		Page<JournalEntry> journalPage=journalRepository.searchByUserAndTags(userId,normalizedTags,normalizedTags.size(),pageRequest);
		List<JournalResponseDTO> entries=journalPage.getContent().stream().map(this::convertToResponseDTO).collect(Collectors.toList());
		
		int pageNumber=journalPage.getNumber();
		int entriesPerPage=journalPage.getSize();
		int totalPages=journalPage.getTotalPages();
		long totalEntries=journalPage.getTotalElements();
		return new PaginatedJournalResponseDTO(entries,pageNumber,entriesPerPage,totalPages,totalEntries);
	

	}
	
	public String exposeEntryToMarkdown(Long entryId)
	{
		StringBuilder markdownBuilder=new StringBuilder();
		Long currentUserId=currentUserService.getCurrentUserId();
		JournalEntry entry=journalRepository.findByIdAndUser_Id(entryId,currentUserId).orElseThrow(() -> new IllegalArgumentException("No user exists with this name"));
		markdownBuilder.append("#"+entry.getEntryTitle());
		markdownBuilder.append("\n\n");
		markdownBuilder.append(entry.getContent()+"\n\n");//im thinking of clearly specifying so if you wanna change content to subject or smething tell me
		markdownBuilder.append("Tags:"+entry.getTags().stream().map(entryTag-> entryTag.getTag().getName()).collect(Collectors.joining(", "))+"\n\n");
		markdownBuilder.append("Created:"+entry.getCreationDate()+"\n\n");
		return markdownBuilder.toString();
	}
	
	
	public void togglePin(Long id) {
	    JournalEntry entry = journalRepository.findByIdAndUser_Id(id, currentUserService.getCurrentUserId())
	            .orElseThrow(() -> new IllegalArgumentException("Entry not found"));

	    entry.setPinned(!entry.isPinned()); // ✅ toggle
	    journalRepository.save(entry);
	}
	
	
	public JournalResponseDTO convertToResponseDTO(JournalEntry entry) {
		return new JournalResponseDTO(
                entry.getId(),
                entry.getEntryTitle(),
                entry.getContent(),
                entry.getCreationDate(),
                entry.getTags().stream().map(entryTag -> entryTag.getTag().getName()).collect(Collectors.toList()),entry.getUpdatedAt(),entry.isPinned()
       
				);
	}
	public List<String> getTagsForUser(String username) {
	    return tagRepository.findDistinctTagNamesByUsername(username);
	}

}
