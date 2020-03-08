package com.strade.service;

import com.strade.dao.GraphDao;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import com.strade.exceptions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.strade.utils.Labels.OWNS_VERB;
import static com.strade.utils.Labels.WANTS_VERB;

public class GraphService {

	private static GraphDao graphDao = GraphDao.getInstance();
	private static GraphService instance;

	Logger logger = Logger.getLogger(GraphService.class.getName());

	//injector for testing
	GraphService(GraphDao graphDao) {
		this.graphDao = graphDao;
	}

	public static GraphService getInstance() {
		if (instance == null)
			instance = new GraphService(graphDao);
		return instance;
	}

	public String aboutPage() throws IOException {
		return "true";
	}

	public void addUser(User user) throws UserException {
		String userId = user.getUuid();
		boolean userExists = graphDao.doesUserExist(userId);
		if (userExists) {
			throw new UserAlreadyExistsException(String.format("User with id %s already exists", userId));
		} else {
			graphDao.createUser(user);
		}
	}

	public void removeUser(String userId) {
		graphDao.deleteUser(userId);
	}

	public User getUser(String userId) throws UserDoesNotExistException {
		User user = graphDao.getUser(userId);
		if (null != user) {
			return user;
		} else {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", userId));
		}
	}

	public void addTextbook(Textbook textbook) throws TextbookException {
		//todo!!! create books by isbn
		String textbookId = textbook.getUuid();
		boolean textbookExists = graphDao.doesTextbookExistById(textbookId);
		if (textbookExists) {
			throw new TextbookAlreadyExistsException(String.format("Textbook with id %s already exists.", textbookId));
		} else {
			boolean textbookCreated = graphDao.createTextbook(textbook);
			if (!textbookCreated) {
				throw new TextbookException(String.format("Textbook with id %s was not able to be created", textbookId));
			}
		}
	}

	public void removeTextbook(String textbookId) {
		graphDao.deleteTextbook(textbookId);
	}

	public Textbook getTextbookById(String textbookId) throws TextbookException {
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		Textbook textbook;
		if (doesTextbookExist) {
			textbook = graphDao.getTextbook(textbookId);
			if (null == textbook) {
				throw new TextbookException(String.format("Textbook with id %s could not be retrieved", textbookId));
			}
		} else {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesn't exist", textbookId));
		}
		return textbook;
	}

	public Textbook getTextbookByIsbn10(String isbn10) throws TextbookException {
		boolean doesTextbookExist = graphDao.doesTextbookExistByIsbn10(isbn10);
		Textbook textbook;
		if (doesTextbookExist) {
			textbook = graphDao.getTextbookIsbn10(isbn10);
			if (null == textbook) {
				throw new TextbookException(String.format("Textbook with id %s could not be retrieved", isbn10));
			}
		} else {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesn't exist", isbn10));
		}
		return textbook;
	}

	public Textbook getTextbookByIsbn13(String isbn13) throws TextbookException {
		boolean doesTextbookExist = graphDao.doesTextbookExistByIsbn10(isbn13);
		Textbook textbook;
		if (doesTextbookExist) {
			textbook = graphDao.getTextbookIsbn13(isbn13);
			if (null == textbook) {
				throw new TextbookException(String.format("Textbook with id %s could not be retrieved", isbn13));
			}
		} else {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesn't exist", isbn13));
		}
		return textbook;
	}

	public void addTextbookRelationship(String userId,
										String verb,
										String textbookId)
			throws UserDoesNotExistException, VerbException, TextbookDoesNotExistException, RelationshipException {

		validateInputsForRelationship(userId, verb, textbookId);
		boolean created = graphDao.createTextbookRelationship(userId, verb, textbookId);
		if (!created) {
			throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
		}
	}

	public void removeTextbookRelationship(String userId,
										   String verb,
										   String textbookId)
			throws TextbookDoesNotExistException, UserDoesNotExistException, VerbException, RelationshipException {
		validateInputsForRelationship(userId, verb, textbookId);
		boolean relationshipDeleted = graphDao.deleteTextbookRelationship(userId, verb, textbookId);
		if (!relationshipDeleted) {
			throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
		}
	}

	public Relationship getTextbookRelationship(String userId,
												String verb,
												String textbookId)
			throws RelationshipException, TextbookDoesNotExistException, UserDoesNotExistException, VerbException {
		validateInputsForRelationship(userId, verb, textbookId);
		Relationship relationship = graphDao.getTextbookRelationship(userId, verb, textbookId);
		if (null != relationship) {
			return relationship;
		}
		throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
	}

	public List<User> findUsersWithTextbook(String textbookId) throws TextbookDoesNotExistException {
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if (!doesTextbookExist) {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesn't exist", textbookId));
		}
		return graphDao.getUsersWhoOwnTextbook(textbookId);
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooks(List<String> textbookIds) throws TextbookDoesNotExistException {
		List<String> existingTextbooks = new ArrayList<>();
		for (String textbookId : textbookIds) {
			if (graphDao.doesTextbookExistById(textbookId)) {
				existingTextbooks.add(textbookId);
			}
		}
		if (existingTextbooks.size() == 0) {
			throw new TextbookDoesNotExistException("None of the supplied textbook ids exist");
		} else {
			return graphDao.getUsersWhoOwnTextbooks(existingTextbooks);
		}
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooksFromWishList(String userId) throws UserDoesNotExistException {
		boolean userExists = graphDao.doesUserExist(userId);
		if (userExists) {
			return graphDao.getUsersWhoOwnWantedTextbooks(userId);
		} else {
			throw new UserDoesNotExistException(String.format("User with id %s doesnt exist", userId));
		}
	}

	public void transferBook(String owner,
							 String consumer,
							 String textbookId)
			throws UserDoesNotExistException, TextbookDoesNotExistException, RelationshipException, UserDoesntOwnTextbookException {
		validateInputsForTransfer(owner, consumer, textbookId);
		Relationship relationship = graphDao.getTextbookRelationship(owner, OWNS_VERB, textbookId);
		if (null == relationship) {
			throw new UserDoesntOwnTextbookException(String.format("Unable to complete transfer. User %s does not own textbook %s", owner, textbookId));
		}
		boolean deleteOwnsRelationship = graphDao.deleteTextbookRelationship(owner, OWNS_VERB, textbookId);
		boolean deleteWantsRelationship = graphDao.deleteTextbookRelationship(consumer, WANTS_VERB, textbookId);
		boolean textbookRelationship = graphDao.createTextbookRelationship(consumer, OWNS_VERB, textbookId);
		if (!textbookRelationship) {
			throw new RelationshipException(String.format("Unable to complete tranfer between owner %s, consumer %s and textbook %s", owner, consumer, textbookId));
		}

	}

	public List<Textbook> getTextbooksByRelationship(String user, String verb) throws UserDoesNotExistException, VerbException {
		validateInputsForUserAndVerb(user, verb);
		return graphDao.getTextbooksByVerb(user, verb);
	}

	private void validateInputsForUserAndVerb(String user, String verb) throws VerbException, UserDoesNotExistException {
		if (!graphDao.doesUserExist(user)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", user));
		}
		if (!graphDao.isVerbValid(verb)) {
			throw new VerbException(String.format("Verb %s is not valid", verb));
		}
	}

	private void validateInputsForTransfer(String owner, String consumer, String textbookId) throws TextbookDoesNotExistException, UserDoesNotExistException {
		if (!graphDao.doesUserExist(owner)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", owner));
		}
		if (!graphDao.doesUserExist(consumer)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", consumer));
		}
		if (!graphDao.doesTextbookExistById(textbookId)) {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesnt exist", textbookId));
		}
	}

	private void validateInputsForRelationship(String userId, String verb, String textbookId) throws UserDoesNotExistException, VerbException, TextbookDoesNotExistException {
		if (!graphDao.doesUserExist(userId)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", userId));
		}
		if (!graphDao.isVerbValid(verb)) {
			throw new VerbException(String.format("Verb %s is not valid", verb));
		}
		if (!graphDao.doesTextbookExistById(textbookId)) {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesnt exist", textbookId));
		}
	}
}
