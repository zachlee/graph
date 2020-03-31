package com.studentrade.base.resource;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.studentrade.base.dao.GraphDaoModule;
import com.studentrade.base.routing.GraphRouting;
import com.studentrade.base.service.GraphServiceModule;
import com.studentrade.base.server.io.Routing;

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
