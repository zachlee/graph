package com.strade.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Verb {
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("name")
	public String name;
}
