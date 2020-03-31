package com.studentrade.graph.server;

import com.google.inject.AbstractModule;
import com.studentrade.graph.modules.GraphModule;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
        bind(Startup.class);
        install(new GraphModule());
    }
}
