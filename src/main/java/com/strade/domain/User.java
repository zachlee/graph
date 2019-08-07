package com.strade.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
	@JsonProperty("uuid")
	private String uuid;

	@JsonProperty("username")
	private String username;

	@JsonProperty("email")
	private String email;

	@JsonProperty("school")
	private String school;

	@JsonProperty("type")
	private String type;

	public User(String uuid,
				String username,
				String email,
				String school,
				String type) {
		setUuid(uuid);
		setUsername(username);
		setEmail(email);
		setSchool(school);
		setType(type);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
