package com.studentrade.graph.server.io.web;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.studentrade.graph.auth.GraphAccessManager;
import com.studentrade.graph.server.io.AppEntrypoint;
import com.studentrade.graph.server.io.EntryPointType;
import io.javalin.Javalin;
import org.jetbrains.annotations.NotNull;

public class WebModule extends AbstractModule {

    private Javalin app;

    private WebModule(Javalin app) {
        this.app = app;
    }

    @NotNull
    public static WebModule create() {
        return new WebModule(Javalin.create(config -> {
        	config.accessManager(new GraphAccessManager());
		}));
    }

    @Override
    protected void configure() {
        bind(Javalin.class).toInstance(app);
        MapBinder.newMapBinder(binder(), EntryPointType.class, AppEntrypoint.class)
				.addBinding(EntryPointType.REST)
				.to(WebEntryPoint.class);
    }
}
