package com.strade.service;

import com.strade.dao.GraphDao;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import com.strade.exceptions.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
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
	private Relationship relationship;

	private static String USER_ONE = UUID.randomUUID().toString();
	private static String USER_TWO = UUID.randomUUID().toString();
	private static String TEXTBOOK_ONE = UUID.randomUUID().toString();
	private static String VERB = "verb";


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
		relationship = new Relationship(USER_ONE,
				VERB,
				TEXTBOOK_ONE);
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
		doReturn(user).when(graphDaoMock).getUser(anyString());
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
	public void addTextbookHappyPath() {
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(true).when(graphDaoMock).createTextbook(any());
		try {
			graphServiceUnderTest.addTextbook(textbook);
		} catch (Exception e) {
			fail("No exception should be thrown here");
		}
	}

	@Test
	public void addTextbookThrowsTextbookAlreadyExistsException() {
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.addTextbook(textbook);
			fail("Expected TextbookAlreadyExists exception");
		} catch (Exception e) {
			assert e instanceof TextbookAlreadyExistsException;
		}
	}

	@Test
	public void addTextbookNotAbleToBeAdded() {
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(false).when(graphDaoMock).createTextbook(any());
		try {
			graphServiceUnderTest.addTextbook(textbook);
			fail("Expected TextbookException exception");
		} catch (Exception e) {
			assert e instanceof TextbookException;
		}
	}

	@Test
	public void getTextbookByIdHappyPath() {
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(textbook).when(graphDaoMock).getTextbook(TEXTBOOK_ONE);
		try {
			graphServiceUnderTest.getTextbookById(TEXTBOOK_ONE);
		} catch (Exception e){
			fail("Should not have thrown exception.");
		}
	}

	@Test
	public void getTextbookByIdTextbookDoesNotExist() {
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.getTextbookById(TEXTBOOK_ONE);
			fail("Should have thrown TextbookDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void getTextbookByIdNothingReturnedFromDao() {
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(null).when(graphDaoMock).getTextbook(anyString());
		try {
			graphServiceUnderTest.getTextbookById(TEXTBOOK_ONE);
			fail("Should have thrown TextbookException");
		} catch (Exception e) {
			assert e instanceof TextbookException;
		}
	}

	@Test
	public void addTextbookRelationshipHappyPath() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(true).when(graphDaoMock)
				.createTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
		} catch (Exception e) {
			fail("Should not have thrown exception");
		}
	}

	@Test
	public void addTextbookRelationshipUserDoesntExist() {
		doReturn(false).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
			fail("Should have thrown UserDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void addTextbookRelationshipVerbNotValid(){
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(false).when(graphDaoMock).isVerbValid(anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
			fail("Should have thrown VerbNotValidException");
		} catch (Exception e) {
			assert e instanceof VerbException;
		}
	}

	@Test
	public void addTextbookRelationshipTextbookDoesntExist() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
			fail("Should have thrown TextbookDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void addTextbookRelationshipCouldNotCreateRelationship() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(false).when(graphDaoMock)
				.createTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
			fail("Should have thrown RelationshipException");
		} catch (Exception e) {
			assert e instanceof RelationshipException;
		}
	}

	@Test
	public void getTextbookRelationshipHappyPath() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(relationship).when(graphDaoMock)
				.getTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.getTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void getTextbookRelationshipUserDoesntExist() {
		doReturn(false).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.getTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have failed with UserDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void getTextbookRelationshipVerbNotValid() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(false).when(graphDaoMock).isVerbValid(anyString());
		try {
			graphServiceUnderTest.getTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have failed with VerbException");
		} catch (Exception e) {
			assert e instanceof VerbException;
		}
	}

	@Test
	public void getTextbookRelationshipTextbookDoesntExist() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.getTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have failed with TextbookDoesntExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void getTextbookRelationshipRelationshipNotCreated() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(null).when(graphDaoMock)
				.getTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.getTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have failed with RelationshipException");
		} catch (Exception e) {
			assert e instanceof RelationshipException;
		}
	}

	@Test
	public void removeTextbookRelationhipHappyPath() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(true).when(graphDaoMock)
				.deleteTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.removeTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void removeTextbookRelationshipUserDoesNotExist() {
		doReturn(false).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.removeTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have thrown UserDoesntExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void removeTextbookRelationshipVerbNotValid(){
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(false).when(graphDaoMock).isVerbValid(anyString());
		try {
			graphServiceUnderTest.removeTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have thrown VerbException");
		} catch (Exception e) {
			assert e instanceof VerbException;
		}
	}

	@Test
	public void removeTextbookRelationshipTextbookDoesntExist() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.removeTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have thrown TextbookDoesntExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void removeTextbookRelationshipNotRemoved() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).isVerbValid(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(false).when(graphDaoMock)
				.deleteTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.removeTextbookRelationship(USER_ONE,
					VERB,
					TEXTBOOK_ONE);
			fail("Should have thrown RelationshipException");
		} catch (Exception e) {
			assert e instanceof RelationshipException;
		}
	}

	@Test
	public void findUsersWithTextbookHappyPath(){
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.findUsersWithTextbook(TEXTBOOK_ONE);
		} catch (Exception e){
			fail("Should not have thrown exception");
		}
	}

	@Test
	public void findUsersWithTextbookTextbookDoesntExist() {
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.findUsersWithTextbook(TEXTBOOK_ONE);
			fail("Should hve thrown TextbookDoesntExistException");
		} catch (Exception e){
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksHappyPath() {
		ArrayList<String> textbookList = new ArrayList<>();
		textbookList.add(TEXTBOOK_ONE);
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.getUsersWhoOwnTextbooks(textbookList);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksNoTextbooksExist() {
		ArrayList<String> textbookList = new ArrayList<>();
		textbookList.add(TEXTBOOK_ONE);
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.getUsersWhoOwnTextbooks(textbookList);
			fail("Should have thrown NoTextbooksExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksFromWishListHappyPath() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.getUsersWhoOwnTextbooksFromWishList(USER_ONE);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksFromWishListUserDoesntExist() {
		doReturn(false).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.getUsersWhoOwnTextbooksFromWishList(USER_ONE);
			fail("Should have thrown an exception");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void transferBookHappyPath() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(relationship).when(graphDaoMock)
				.getTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(true).when(graphDaoMock)
				.deleteTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(true).when(graphDaoMock)
				.createTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void transferBookOwnerDoesntExist() {
		doReturn(false).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected UserDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void transferBookConsumerDoesntExist() {
		doReturn(false).when(graphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected UserDoesntExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void tranferBookTextbookDoesntExist() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(false).when(graphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected TextbookDoesntExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void transferBookUserDoesntOwnTextbook(){
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(null).when(graphDaoMock)
				.getTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected UserDoesNotOwnTextbookException");
		} catch (Exception e) {
			assert e instanceof UserDoesntOwnTextbookException;
		}
	}

	@Test
	public void transferBookCouldNotCreateOwnsTextbookRelationship() {
		doReturn(true).when(graphDaoMock).doesUserExist(anyString());
		doReturn(true).when(graphDaoMock).doesTextbookExistById(anyString());
		doReturn(relationship).when(graphDaoMock)
				.getTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(true).when(graphDaoMock)
				.deleteTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(false).when(graphDaoMock)
				.createTextbookRelationship(anyString(),
						anyString(),
						anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected RelationshipException");
		} catch (Exception e) {
			assert e instanceof RelationshipException;
		}
	}
}
