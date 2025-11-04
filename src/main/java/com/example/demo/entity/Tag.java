package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name="tags",indexes= {@Index(name="idx_tag_name",columnList="name",unique=true)})
public class Tag {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
@Column(unique=true,nullable=false,length=100)
	
	private String name;
	
	
	Tag(){}
	public Tag(String name)
	{
		this.name = name.toLowerCase();
		
	}
	public void setName(String name)
	{
		this.name=name.toLowerCase();
		
	}
	public void setId(Long id)
	{
		this.id=id;
	}
	public String getName()
	{
		return name;
	}
	public long getId()
	{
		return id;
	}
}
