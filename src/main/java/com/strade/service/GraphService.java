package com.strade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

public class GraphService {

	Logger logger = Logger.getLogger(GraphService.class.getName());
	private static GraphDao graphDao = GraphDao.getInstance();
	private static GraphService instance;
	ObjectMapper mapper = new ObjectMapper();

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

	public boolean addUser(User user) throws UserException {
		String userId = user.getUuid();
		boolean userExists = graphDao.doesUserExist(userId);
		if (userExists) {
			throw new UserAlreadyExistsException(String.format("User with id %s already exists", userId));
		} else {
			return graphDao.createUser(user);
		}
	}

	public boolean removeUser(String userId) {
		return graphDao.deleteUser(userId);
	}

	public User getUser(String userId) {
		return graphDao.getUser(userId);
	}

	public boolean addTextbook(Textbook textbook) throws Exception {
		String textbookId = textbook.getUuid();
		boolean textbookExists = graphDao.doesTextbookExistById(textbookId);
		if (textbookExists) {
			throw new TextbookAlreadyExistsException(String.format("Textbook with id %s already exists.", textbookId));
		} else {
			return graphDao.createTextbook(textbook);
		}
	}

	public boolean removeTextbook(String textbookId) {
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if (doesTextbookExist) {
			return graphDao.deleteTextbook(textbookId);
		} else {
			return false;
		}
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

	public boolean addTextbookRelationship(String userId,
										   String verb,
										   String textbookId)
			throws UserDoesNotExistException, VerbException, TextbookDoesNotExistException, RelationshipException {

		validateInputsForRelationship(userId, verb, textbookId);
		boolean created = graphDao.createTextbookRelationship(userId, verb, textbookId);
		if ( !created ) {
			throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
		}
		return created;
	}

	public boolean removeTextbookRelationship(String userId,
											  String verb,
											  String textbookId)
			throws TextbookDoesNotExistException, UserDoesNotExistException, VerbException, RelationshipException {
		validateInputsForRelationship(userId, verb, textbookId);
		boolean relationshipDeleted = graphDao.deleteTextbookRelationship(userId, verb, textbookId);
		if (!relationshipDeleted) {
			throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
		}
		return relationshipDeleted;
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

	public List<User> findUsersWithTextbook(String textbookId) {
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if (!doesTextbookExist) {
			throw new TextbookDoesNotExistException()
			return graphDao.getUsersWhoOwnTextbook(textbookId);
		} else {
			return null;
		}
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooks(List<String> textbookIds) {
		List<String> existingTextbooks = new ArrayList<>();
		for (String textbookId : textbookIds) {
			if (graphDao.doesTextbookExistById(textbookId)) {
				existingTextbooks.add(textbookId);
			}
		}
		if (existingTextbooks.size() > 0) {
			return graphDao.getUsersWhoOwnTextbooks(existingTextbooks);
		} else {
			return null;
		}
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooksFromWishList(String userId) {
		boolean userExists = graphDao.doesUserExist(userId);
		if (userExists) {
			Map<Long, List<User>> usersWhoOwnWantedTextbooks = graphDao.getUsersWhoOwnWantedTextbooks(userId);
			if (usersWhoOwnWantedTextbooks.size() == 0) {
				return null;
			} else {
				return usersWhoOwnWantedTextbooks;
			}
		} else {
			return null;
		}
	}

	public boolean transferBook(String owner, String consumer, String textbookId) {
		boolean ownerExists = graphDao.doesUserExist(owner);
		boolean consumerExists = graphDao.doesUserExist(consumer);
		boolean textbookExists = graphDao.doesTextbookExistById(textbookId);
		if (ownerExists && consumerExists && textbookExists) {
			boolean textbookTransferred = graphDao.transferTextbookBetweenUsers(owner, consumer, textbookId);
			return textbookTransferred;
		} else {
			return false;
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
