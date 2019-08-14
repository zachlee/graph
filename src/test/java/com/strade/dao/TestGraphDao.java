package com.strade.dao;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Logger;

public class TestGraphDao {

	Logger logger = Logger.getLogger(TestGraphDao.class.getName());
	private static GraphDao graphDao;

	@BeforeClass
	public static void setup() {
		graphDao = new GraphDao();
	}
}
