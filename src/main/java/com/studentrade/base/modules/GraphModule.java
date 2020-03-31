package com.studentrade.base.modules;

import com.google.inject.AbstractModule;
import com.studentrade.base.resource.GraphResourceModule;
import com.studentrade.base.server.io.web.WebModule;

public class GraphModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new GraphResourceModule());
		install(WebModule.create());
	}
}