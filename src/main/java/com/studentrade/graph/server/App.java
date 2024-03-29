package com.studentrade.graph.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.studentrade.graph.server.io.EntryPointType;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {
		Injector injector = Guice.createInjector(new AppModule());
		injector.getInstance(Startup.class).boot(EntryPointType.REST, args);
    }
}
