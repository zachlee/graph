package com.studentrade.base.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.studentrade.base.dao.GraphDao;

@Singleton
public class GraphServiceImpl implements GraphService {

	private final GraphDao graphDao;

	@Inject
	public GraphServiceImpl(GraphDao graphDao) {
		this.graphDao = graphDao;
	}

}
