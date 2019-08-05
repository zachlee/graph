package com.strade.service;

import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GraphServiceTest {
	@Mock
	private GraphDao graphDaoMock;

	private GraphService underTest;

	private Textbook textbook;

	@Before
	public void setup() {
		textbook = new Textbook("id",
				"title",
				"author",
				"generalSubject",
				"specificSubject",
				"isbn10",
				"isbn13");
		underTest = new GraphService(graphDaoMock);
	}

	@Test
	public void addTextbookWhenTextbookAlreadyExists() {
//		when(graphDaoMock.doesTextbookExist(anyString())).thenReturn(true);
		when(graphDaoMock.insertTextbook(textbook)).thenThrow(Exception.class);
		try {
			underTest.addTextbook(textbook);
			fail("Test should have thrown exception for textbook already existing");
		} catch ( Exception e ) {
			assertTrue(e instanceof Exception);
		}
	}

	@Test
	public void successfullyAddTextbook() throws Exception {
//		when(graphDaoMock.doesTextbookExist(anyString())).thenReturn(false);
		when(graphDaoMock.insertTextbook(any(Textbook.class))).thenReturn(true);
		boolean textbookAdded = underTest.addTextbook(textbook);
		assertTrue(textbookAdded);
	}
}
