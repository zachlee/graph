package com.studentrade.base.dao;

import com.google.inject.AbstractModule;

public class GraphDaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(GraphDao.class).to(GraphDaoImpl.class);
	}
}
