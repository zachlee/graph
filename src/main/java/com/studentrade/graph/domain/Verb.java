package com.studentrade.graph.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Verb {
	@JsonProperty("name")
	private String name;

	public Verb() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
