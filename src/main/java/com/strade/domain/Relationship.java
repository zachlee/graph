package com.strade.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Relationship {

	@JsonProperty("uuid")
	private String uuid;

	@JsonProperty("verb")
	private String verb;

	@JsonProperty("user")
	private String user;

	@JsonProperty("textbook")
	private String textbook;

	public Relationship(){}

	public Relationship(String user,
						String verb,
						String textbook) {
		setUser(user);
		setVerb(verb);
		setTextbook(textbook);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getTextbook() {
		return textbook;
	}

	public void setTextbook(String textbook) {
		this.textbook = textbook;
	}
}
