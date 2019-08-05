package com.strade.functional;

import com.strade.dao.GraphDao;
import org.junit.Test;

public class FunctionalDao {
	GraphDao graphDao = GraphDao.getInstance();

	@Test
	public void testGetTextbookOr() {
		//todo automate this fully
		boolean textbookExist = graphDao.doesTextbookExist("0", "1", "4");
		assert textbookExist;
	}

	@Test
	public void testGetTextbookById() {
		//todo automate
		boolean textbookExists = graphDao.doesTextbookExistById("1");
		assert textbookExists;
	}
}
