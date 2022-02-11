package com.studentrade.graph.dao;

import com.google.inject.AbstractModule;

public class TextbookGraphDaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(TextbookGraphDao.class).to(TextbookGraphDaoImpl.class);
	}
}
