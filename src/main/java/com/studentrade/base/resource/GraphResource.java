package com.studentrade.base.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.studentrade.base.service.GraphService;

@Singleton
public class GraphResource {

	private GraphService graphService;

	@Inject
	public GraphResource(GraphService graphService) {
		this.graphService = graphService;
	}
}