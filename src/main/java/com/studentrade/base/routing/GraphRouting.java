package com.studentrade.base.routing;

import com.studentrade.base.resource.GraphResource;
import com.studentrade.base.server.io.Routing;
import io.javalin.Javalin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GraphRouting extends Routing<GraphResource> {

	private Javalin javalin;

	private static final String APP_BASE_PATH = "";

	@Inject
	public GraphRouting(Javalin javalin) {
		this.javalin = javalin;
	}

	@Override
	public void bindRoutes() {
		javalin.routes(() -> {});
	}
}
