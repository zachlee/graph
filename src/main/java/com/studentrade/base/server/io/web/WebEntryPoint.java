package com.studentrade.base.server.io.web;

import com.google.inject.Inject;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.studentrade.base.server.io.AppEntrypoint;
import com.studentrade.base.server.io.Routing;
import io.javalin.Javalin;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.Set;

@Singleton
class WebEntryPoint implements AppEntrypoint {

	private static final String PORT_PROPERTY = "service.port";
	private static final DynamicIntProperty PORT = DynamicPropertyFactory.getInstance().getIntProperty(PORT_PROPERTY, 7000);

    private Javalin app;

    @Inject(optional = true)
    private Set<Routing> routes = Collections.emptySet();

    @Inject
    public WebEntryPoint(Javalin app) {
        this.app = app;
    }

    @Override
    public void boot(String[] args) {
        bindRoutes();
        app.start(PORT.get());
    }

    private void bindRoutes() {
        routes.forEach(Routing::bindRoutes);
    }
}
