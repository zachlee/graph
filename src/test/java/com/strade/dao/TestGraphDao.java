package com.strade.dao;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestGraphDao {

	private static GraphDao graphDao;

	@BeforeClass
	public static void setup() {
		graphDao = new GraphDao();
	}
}
