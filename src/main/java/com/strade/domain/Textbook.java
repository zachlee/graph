package com.strade.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

public class Textbook {
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
