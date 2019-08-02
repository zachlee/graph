package com.strade.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Textbook {

	@JsonProperty("id")
	private UUID id;

	@JsonProperty("title")
	private String title;

	@JsonProperty("author")
	private String author;

	@JsonProperty("generalSubject")
	private String generalSubject;

	@JsonProperty("specificSubject")
	private String specificSubject;

	@JsonProperty("isbn10")
	private String isbn10;

	@JsonProperty("isbn13")
	private String isbn13;

	public Textbook(UUID id,
					String title,
					String author,
					String generalSubject,
					String specificSubject,
					String isbn10,
					String isbn13) {
		this.setTitle(title);
		this.setAuthor(author);
		this.setGeneralSubject(generalSubject);
		this.setSpecificSubject(specificSubject);
		this.setIsbn10(isbn10);
		this.setIsbn13(isbn13);
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id){
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getGeneralSubject() {
		return generalSubject;
	}

	public void setGeneralSubject(String generalSubject) {
		this.generalSubject = generalSubject;
	}

	public String getSpecificSubject() {
		return specificSubject;
	}

	public void setSpecificSubject(String specificSubject) {
		this.specificSubject = specificSubject;
	}

	public String getIsbn10() {
		return isbn10;
	}

	public void setIsbn10(String isbn10) {
		this.isbn10 = isbn10;
	}

	public String getIsbn13() {
		return isbn13;
	}

	public void setIsbn13(String isbn13) {
		this.isbn13 = isbn13;
	}
}
