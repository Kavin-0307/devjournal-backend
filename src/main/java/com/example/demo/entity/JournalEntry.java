package com.example.demo.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;//import necessary packages
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
@Entity//Entity is basically A POJO
@Table(name="journal")
public class JournalEntry {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="EntryTitle",nullable=false,length=100)//column annotation set to nullable
	private String entryTitle;//stores name of entry title
	
	@Column(columnDefinition="text")//here columndef allows us to store large blocks of text easily
	private String content;
	
	@Column(name="created_at")
	@CreationTimestamp//Stores when it was created
	private  LocalDateTime creationDate;
	
	@OneToMany(mappedBy="journalEntry",cascade= CascadeType.ALL,orphanRemoval=true)
	private Set<EntryTag> tags=new HashSet<>();
	@ManyToOne(optional=false,fetch=FetchType.LAZY)
	@JoinColumn(name="UserId")
	@JsonIgnore
	private User user;
	@UpdateTimestamp
	private Date updatedAt;
	@Column(nullable = false)
	private boolean pinned = false;

	public boolean isPinned() { return pinned; }
	public void setPinned(boolean pinned) { this.pinned = pinned; }

	public JournalEntry()//must have no-args constructor
	{
		
		
	}
	JournalEntry(String entryTitle,String content)//allows us to enter during obj creation
	{
		this.entryTitle=entryTitle;
		this.content=content;
	}
	//standard getter
	public Set<EntryTag> getTags()
	{
		return tags;
	}
	public Long getId()
	{
		return id;
	}
	public void setTags(Set<EntryTag> tags)
	{
		this.tags=tags;
	}
	public String getEntryTitle()
	{
		return entryTitle;
	}
	public String getContent()
	{
		return content;
	}
	public LocalDateTime getCreationDate()
	{
		return creationDate;
	}
	//standard setters
	public void setEntryTitle(String entryTitle)
	{
		this.entryTitle=entryTitle;
	}
	public void setContent(String content)
	{
		this.content=content;
	}
	public void setCreationDate(LocalDateTime creationDate)
	{
		this.creationDate=creationDate;
	}
	
	public void setUser(User user)
	{
		this.user=user;
	}
	public User getUser()
	{
		return user;
	}
	@Override
	public String toString() {
	    return "JournalEntry{" +
	            "id=" + id +
	            ", entryTitle='" + entryTitle + '\'' +
	            ", content='" + content + '\'' +
	            ", creationDate=" + creationDate +
	            '}';
	}
	public Date getUpdatedAt() {
	    return updatedAt;
	}


}
