package com.studentrade.graph.unit;

import com.studentrade.graph.dao.TextbookGraphDao;
import com.studentrade.graph.domain.Relationship;
import com.studentrade.graph.domain.Textbook;
import com.studentrade.graph.domain.User;
import com.studentrade.graph.exception.*;
import com.studentrade.graph.service.GraphService;
import com.studentrade.graph.service.GraphServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static com.studentrade.graph.util.Labels.*;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GraphServiceTest {

	private TextbookGraphDao textbookGraphDaoMock;

	private GraphService graphServiceUnderTest;

	private Textbook textbook;
	private User user;
	private Relationship relationship;

	private static String USER_ONE = UUID.randomUUID().toString();
	private static String USER_TWO = UUID.randomUUID().toString();
	private static String TEXTBOOK_ONE = UUID.randomUUID().toString();
	private static String VERB = "verb";


	@BeforeEach
	public void setup() {
		textbookGraphDaoMock = mock(TextbookGraphDao.class);
		textbook = new Textbook(NODE_UUID,
				TITLE,
				AUTHOR,
				ISBN10,
				ISBN13,
				IMAGE_LINK);
		user = new User(NODE_UUID,
				USERNAME,
				EMAIL,
				SCHOOL,
				TYPE);
		relationship = new Relationship(USER_ONE,
				VERB,
				TEXTBOOK_ONE);

		graphServiceUnderTest = new GraphServiceImpl(textbookGraphDaoMock);
	}

	@Test
	public void addUserHappyPath() throws UserException {
		doReturn(false).when(textbookGraphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.addUser(user);
		} catch (Exception e) {
			fail("There should have been no exception thrown.");
		}
	}

	@Test
	public void addUserWhenUserExistsThrowsUserAlreadyExistsException() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.addUser(user);
			fail("Should have thrown UserAlreadyExistsException");
		} catch (Exception e) {
			assert e instanceof UserAlreadyExistsException;
		}
	}

	@Test
	public void getUserHappyPath() {
		doReturn(user).when(textbookGraphDaoMock).getUser(anyString());
		try {
			User user = graphServiceUnderTest.getUser(USER_ONE);
			assert null != user;
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void getUserReturnsNull() {
		doReturn(null).when(textbookGraphDaoMock).getUser(anyString());
		try {
			graphServiceUnderTest.getUser(USER_ONE);
			fail("Should have thrown UserDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void addTextbookHappyPath() {
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(true).when(textbookGraphDaoMock).createTextbook(any());
		try {
			graphServiceUnderTest.addTextbook(textbook);
		} catch (Exception e) {
			fail("No exception should be thrown here");
		}
	}

	@Test
	public void addTextbookThrowsTextbookAlreadyExistsException() {
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.addTextbook(textbook);
			fail("Expected TextbookAlreadyExists exception");
		} catch (Exception e) {
			assert e instanceof TextbookAlreadyExistsException;
		}
	}

	@Test
	public void addTextbookNotAbleToBeAdded() {
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(false).when(textbookGraphDaoMock).createTextbook(any());
		try {
			graphServiceUnderTest.addTextbook(textbook);
			fail("Expected TextbookException exception");
		} catch (Exception e) {
			assert e instanceof TextbookException;
		}
	}

	@Test
	public void getTextbookByIdHappyPath() {
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(textbook).when(textbookGraphDaoMock).getTextbook(TEXTBOOK_ONE);
		try {
			graphServiceUnderTest.getTextbookById(TEXTBOOK_ONE);
		} catch (Exception e) {
			fail("Should not have thrown exception.");
		}
	}

	@Test
	public void getTextbookByIdTextbookDoesNotExist() {
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.getTextbookById(TEXTBOOK_ONE);
			fail("Should have thrown TextbookDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void getTextbookByIdNothingReturnedFromDao() {
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(null).when(textbookGraphDaoMock).getTextbook(anyString());
		try {
			graphServiceUnderTest.getTextbookById(TEXTBOOK_ONE);
			fail("Should have thrown TextbookException");
		} catch (Exception e) {
			assert e instanceof TextbookException;
		}
	}

	@Test
	public void addTextbookRelationshipHappyPath() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(true).when(textbookGraphDaoMock)
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
		doReturn(false).when(textbookGraphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
			fail("Should have thrown UserDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void addTextbookRelationshipVerbNotValid() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(false).when(textbookGraphDaoMock).isVerbValid(anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
			fail("Should have thrown VerbNotValidException");
		} catch (Exception e) {
			assert e instanceof VerbException;
		}
	}

	@Test
	public void addTextbookRelationshipTextbookDoesntExist() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.addTextbookRelationship(USER_ONE, VERB, TEXTBOOK_ONE);
			fail("Should have thrown TextbookDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void addTextbookRelationshipCouldNotCreateRelationship() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(false).when(textbookGraphDaoMock)
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(relationship).when(textbookGraphDaoMock)
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
		doReturn(false).when(textbookGraphDaoMock).doesUserExist(anyString());
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(false).when(textbookGraphDaoMock).isVerbValid(anyString());
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(null).when(textbookGraphDaoMock)
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(true).when(textbookGraphDaoMock)
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
		doReturn(false).when(textbookGraphDaoMock).doesUserExist(anyString());
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
	public void removeTextbookRelationshipVerbNotValid() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(false).when(textbookGraphDaoMock).isVerbValid(anyString());
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).isVerbValid(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(false).when(textbookGraphDaoMock)
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
	public void findUsersWithTextbookHappyPath() {
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.findUsersWithTextbook(TEXTBOOK_ONE);
		} catch (Exception e) {
			fail("Should not have thrown exception");
		}
	}

	@Test
	public void findUsersWithTextbookTextbookDoesntExist() {
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.findUsersWithTextbook(TEXTBOOK_ONE);
			fail("Should hve thrown TextbookDoesntExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksHappyPath() {
		ArrayList<String> textbookList = new ArrayList<>();
		textbookList.add(TEXTBOOK_ONE);
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
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
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.getUsersWhoOwnTextbooks(textbookList);
			fail("Should have thrown NoTextbooksExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksFromWishListHappyPath() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.getUsersWhoOwnTextbooksFromWishList(USER_ONE);
		} catch (Exception e) {
			fail("Should not have thrown an exception");
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksFromWishListUserDoesntExist() {
		doReturn(false).when(textbookGraphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.getUsersWhoOwnTextbooksFromWishList(USER_ONE);
			fail("Should have thrown an exception");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void transferBookHappyPath() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(relationship).when(textbookGraphDaoMock)
				.getTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(true).when(textbookGraphDaoMock)
				.deleteTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(true).when(textbookGraphDaoMock)
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
		doReturn(false).when(textbookGraphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected UserDoesNotExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void transferBookConsumerDoesntExist() {
		doReturn(false).when(textbookGraphDaoMock).doesUserExist(anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected UserDoesntExistException");
		} catch (Exception e) {
			assert e instanceof UserDoesNotExistException;
		}
	}

	@Test
	public void tranferBookTextbookDoesntExist() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(false).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		try {
			graphServiceUnderTest.transferBook(USER_ONE, USER_TWO, TEXTBOOK_ONE);
			fail("Expected TextbookDoesntExistException");
		} catch (Exception e) {
			assert e instanceof TextbookDoesNotExistException;
		}
	}

	@Test
	public void transferBookUserDoesntOwnTextbook() {
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(null).when(textbookGraphDaoMock)
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
		doReturn(true).when(textbookGraphDaoMock).doesUserExist(anyString());
		doReturn(true).when(textbookGraphDaoMock).doesTextbookExistById(anyString());
		doReturn(relationship).when(textbookGraphDaoMock)
				.getTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(true).when(textbookGraphDaoMock)
				.deleteTextbookRelationship(anyString(),
						anyString(),
						anyString());
		doReturn(false).when(textbookGraphDaoMock)
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
