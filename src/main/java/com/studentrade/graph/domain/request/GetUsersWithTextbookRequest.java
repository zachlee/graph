package com.studentrade.graph.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GetUsersWithTextbookRequest {

	@JsonProperty("textbooks")
	List<String> textbooks;

	public GetUsersWithTextbookRequest() {
	}

	public List<String> getTextbooks() {
		return textbooks;
	}
}
