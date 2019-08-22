package com.strade.service;

import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import com.strade.exceptions.UserAlreadyExistsException;
import com.strade.exceptions.UserDoesNotExistException;
import com.strade.exceptions.UserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;
import java.util.logging.Logger;

import static com.strade.utils.Labels.*;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GraphServiceTest {
	Logger logger = Logger.getLogger(GraphServiceTest.class.getName());

	@Mock
	private GraphDao graphDaoMock;

	private GraphService graphServiceUnderTest;

	private Textbook textbook;
	private User user;

	private static String USER_ONE = UUID.randomUUID().toString();

	@Before
	public void setup() {
		textbook = new Textbook(NODE_UUID,
				TITLE,
				AUTHOR,
				GENERAL_SUBJECT,
				SPECIFIC_SUBJECT,
				ISBN10,
				ISBN13);
		user = new User(NODE_UUID,
				USERNAME,
				EMAIL,
				SCHOOL,
				TYPE);
		graphServiceUnderTest = new GraphService(graphDaoMock);
	}

	@Test
	public void addUserHappyPath() throws UserException {
		doReturn(false).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.addUser(user);
		} catch (Exception e) {
			fail("There should have been no exception thrown.");
		}
	}

	@Test
	public void addUserWhenUserExistsThrowsUserAlreadyExistsException() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.addUser(user);
			fail("Should have thrown UserAlreadyExistsException");
		} catch (Exception e) {
			assert e instanceof UserAlreadyExistsException;
		}
	}

	@Test
	public void getUserHappyPath() {
		doReturn(user).when(graphDaoMock.getUser(anyString()));
		try {
			graphServiceUnderTest.getUser(USER_ONE);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void getUserReturnsNull() {
		doReturn(null).when(graphDaoMock).getUser(anyString());
		try {
			graphServiceUnderTest.getUser(USER_ONE);
			fail("Should have thrown UserDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void addTextbookWhenTextbookAlreadyExists() {
//		when(graphDaoMock.doesTextbookExistById(anyString())).thenReturn(true);
		when(graphDaoMock.createTextbook(textbook)).thenThrow(Exception.class);
		try {
			graphServiceUnderTest.addTextbook(textbook);
			fail("Test should have thrown exception for textbook already existing");
		} catch ( Exception e ) {
			assertTrue(e instanceof Exception);
		}
	}

	@Test
	public void successfullyAddTextbook() throws Exception {
//		when(graphDaoMock.doesTextbookExistById(anyString())).thenReturn(false);
		when(graphDaoMock.createTextbook(any(Textbook.class))).thenReturn(true);
		graphServiceUnderTest.addTextbook(textbook);
	}
}
