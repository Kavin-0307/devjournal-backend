package com.example.demo.dto;
//This is for inputting only
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;




public class JournalRequestDTO {
	@NotNull(message="Entry Title is mandatory")
	@Size(min=0,max=100,message="Entry title must be less than 100 characters")
	private String entryTitle;
	
	
	@Size(min=10,max=1000,message="Content must be between 10 and 1000 characters")
	private String content;
	
	
	private LocalDateTime creationDate;
	
	private List<String> tags;
	public List<String> getTags()
	{
		return tags;
	}
	public void setTags(List<String> tags)
	{
		this.tags= tags;
	}
	public String getEntryTitle()
	{
		return entryTitle;
	}
	
	public void setEntryTitle(String entryTitle)
	{
		this.entryTitle=entryTitle;
	}
	public String getContent()
	{
		return content;
		
	}
	public void setContent(String content)
	{
		this.content=content;
	}
	public LocalDateTime getCreationDate()
	{
		return creationDate;
	}
	public void setCreationDate(LocalDateTime creationDate)
	{
		this.creationDate=creationDate;
	}
}
