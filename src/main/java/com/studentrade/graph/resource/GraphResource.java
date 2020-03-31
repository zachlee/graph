package com.studentrade.graph.resource;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.studentrade.graph.service.GraphService;

@Singleton
public class GraphResource {

	private GraphService graphService;

	@Inject
	public GraphResource(GraphService graphService) {
		this.graphService = graphService;
	}
}