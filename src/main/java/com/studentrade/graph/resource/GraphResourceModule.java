package com.studentrade.graph.resource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.studentrade.graph.dao.GraphDaoModule;
import com.studentrade.graph.routing.GraphRouting;
import com.studentrade.graph.service.GraphServiceModule;
import com.studentrade.graph.server.io.Routing;

public class GraphResourceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GraphResource.class);
		install(new GraphServiceModule());
		install(new GraphDaoModule());
		Multibinder.newSetBinder(binder(), Routing.class)
				.addBinding().to(GraphRouting.class);
	}
}
