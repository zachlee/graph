package com.studentrade.graph.dao;

import com.google.inject.AbstractModule;

public class GraphDaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GraphDao.class).to(GraphDaoImpl.class);
	}
}
