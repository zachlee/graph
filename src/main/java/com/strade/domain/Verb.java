package com.strade.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Verb {
	@JsonProperty("name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
