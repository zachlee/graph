package com.studentrade.graph.modules;

import com.google.inject.AbstractModule;
import com.studentrade.graph.resource.GraphResourceModule;
import com.studentrade.graph.server.io.web.WebModule;

public class GraphModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GraphResourceModule());
		install(WebModule.create());
	}
}