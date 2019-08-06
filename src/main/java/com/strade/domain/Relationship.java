package com.strade.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Relationship {

	@JsonProperty("uuid")
	private String uuid;

	private Verb verb;

	private User user;

	private Textbook textbook;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Verb getVerb() {
		return verb;
	}

	public void setVerb(Verb verb) {
		this.verb = verb;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Textbook getTextbook() {
		return textbook;
	}

	public void setTextbook(Textbook textbook) {
		this.textbook = textbook;
	}
}
