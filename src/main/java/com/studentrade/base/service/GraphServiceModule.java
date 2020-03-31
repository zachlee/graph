package com.studentrade.base.service;

import com.google.inject.AbstractModule;

public class GraphServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GraphService.class).to(GraphServiceImpl.class);
	}
}
