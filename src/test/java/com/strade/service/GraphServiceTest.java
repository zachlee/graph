package com.strade.service;

import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.logging.Logger;

import static com.strade.utils.Labels.*;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GraphServiceTest {
	Logger logger = Logger.getLogger(GraphServiceTest.class.getName());

	@Mock
	private GraphDao graphDaoMock;

	private GraphService underTest;

	private Textbook textbook;

	@Before
	public void setup() {
		textbook = new Textbook(NODE_UUID,
				TITLE,
				AUTHOR,
				GENERAL_SUBJECT,
				SPECIFIC_SUBJECT,
				ISBN10,
				ISBN13);
		underTest = new GraphService(graphDaoMock);
	}

	@Test
	public void addTextbookWhenTextbookAlreadyExists() {
//		when(graphDaoMock.doesTextbookExistById(anyString())).thenReturn(true);
		when(graphDaoMock.createTextbook(textbook)).thenThrow(Exception.class);
		try {
			underTest.addTextbook(textbook);
			fail("Test should have thrown exception for textbook already existing");
		} catch ( Exception e ) {
			assertTrue(e instanceof Exception);
		}
	}

	@Test
	public void successfullyAddTextbook() throws Exception {
//		when(graphDaoMock.doesTextbookExistById(anyString())).thenReturn(false);
		when(graphDaoMock.createTextbook(any(Textbook.class))).thenReturn(true);
		boolean textbookAdded = underTest.addTextbook(textbook);
		assertTrue(textbookAdded);
	}
}
