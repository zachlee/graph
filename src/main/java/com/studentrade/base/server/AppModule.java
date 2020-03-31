package com.studentrade.base.server;

import com.google.inject.AbstractModule;
import com.studentrade.base.modules.GraphModule;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
        bind(Startup.class);
        install(new GraphModule());
    }
}
