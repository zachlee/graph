package com.studentrade.graph.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.studentrade.graph.dao.TextbookGraphDao;
import com.studentrade.graph.domain.Relationship;
import com.studentrade.graph.domain.Textbook;
import com.studentrade.graph.domain.User;
import com.studentrade.graph.exception.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.studentrade.graph.util.Labels.OWNS_VERB;
import static com.studentrade.graph.util.Labels.WANTS_VERB;

@Singleton
public class GraphServiceImpl implements GraphService {

	private final TextbookGraphDao textbookGraphDao;

	@Inject
	public GraphServiceImpl(TextbookGraphDao textbookGraphDao) {
		this.textbookGraphDao = textbookGraphDao;
	}

	public void addUser(User user) throws UserException {
		String userId = user.getUuid();
		boolean userExists = textbookGraphDao.doesUserExist(userId);
		if (userExists) {
			throw new UserAlreadyExistsException(String.format("User with id %s already exists", userId));
		} else {
			textbookGraphDao.createUser(user);
		}
	}

	public void removeUser(String userId) {
		textbookGraphDao.deleteUser(userId);
	}

	public User getUser(String userId) throws UserDoesNotExistException {
		User user = textbookGraphDao.getUser(userId);
		if (null != user) {
			return user;
		} else {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", userId));
		}
	}

	public void addTextbook(Textbook textbook) throws TextbookException {
		//todo!!! create books by isbn
		String textbookId = textbook.getUuid();
		boolean textbookExists = textbookGraphDao.doesTextbookExistById(textbookId);
		if (textbookExists) {
			throw new TextbookAlreadyExistsException(String.format("Textbook with id %s already exists.", textbookId));
		} else {
			boolean textbookCreated = textbookGraphDao.createTextbook(textbook);
			if (!textbookCreated) {
				throw new TextbookException(String.format("Textbook with id %s was not able to be created", textbookId));
			}
		}
	}

	public void removeTextbook(String textbookId) {
		textbookGraphDao.deleteTextbook(textbookId);
	}

	public Textbook getTextbookById(String textbookId) throws TextbookException {
		boolean doesTextbookExist = textbookGraphDao.doesTextbookExistById(textbookId);
		Textbook textbook;
		if (doesTextbookExist) {
			textbook = textbookGraphDao.getTextbook(textbookId);
			if (null == textbook) {
				throw new TextbookException(String.format("Textbook with id %s could not be retrieved", textbookId));
			}
		} else {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesn't exist", textbookId));
		}
		return textbook;
	}

	public Textbook getTextbookByIsbn10(String isbn10) throws TextbookException {
		boolean doesTextbookExist = textbookGraphDao.doesTextbookExistByIsbn10(isbn10);
		Textbook textbook;
		if (doesTextbookExist) {
			textbook = textbookGraphDao.getTextbookIsbn10(isbn10);
			if (null == textbook) {
				throw new TextbookException(String.format("Textbook with id %s could not be retrieved", isbn10));
			}
		} else {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesn't exist", isbn10));
		}
		return textbook;
	}

	public Textbook getTextbookByIsbn13(String isbn13) throws TextbookException {
		boolean doesTextbookExist = textbookGraphDao.doesTextbookExistByIsbn10(isbn13);
		Textbook textbook;
		if (doesTextbookExist) {
			textbook = textbookGraphDao.getTextbookIsbn13(isbn13);
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
		boolean created = textbookGraphDao.createTextbookRelationship(userId, verb, textbookId);
		if (!created) {
			throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
		}
	}

	public void removeTextbookRelationship(String userId,
										   String verb,
										   String textbookId)
			throws TextbookDoesNotExistException, UserDoesNotExistException, VerbException, RelationshipException {
		validateInputsForRelationship(userId, verb, textbookId);
		boolean relationshipDeleted = textbookGraphDao.deleteTextbookRelationship(userId, verb, textbookId);
		if (!relationshipDeleted) {
			throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
		}
	}

	public Relationship getTextbookRelationship(String userId,
												String verb,
												String textbookId)
			throws RelationshipException, TextbookDoesNotExistException, UserDoesNotExistException, VerbException {
		validateInputsForRelationship(userId, verb, textbookId);
		Relationship relationship = textbookGraphDao.getTextbookRelationship(userId, verb, textbookId);
		if (null != relationship) {
			return relationship;
		}
		throw new RelationshipException(String.format("Could not create Relationship between %s, %s and %s", userId, verb, textbookId));
	}

	public List<User> findUsersWithTextbook(String textbookId) throws TextbookDoesNotExistException {
		boolean doesTextbookExist = textbookGraphDao.doesTextbookExistById(textbookId);
		if (!doesTextbookExist) {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesn't exist", textbookId));
		}
		return textbookGraphDao.getUsersWhoOwnTextbook(textbookId);
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooks(List<String> textbookIds) throws TextbookDoesNotExistException {
		List<String> existingTextbooks = new ArrayList<>();
		for (String textbookId : textbookIds) {
			if (textbookGraphDao.doesTextbookExistById(textbookId)) {
				existingTextbooks.add(textbookId);
			}
		}
		if (existingTextbooks.size() == 0) {
			throw new TextbookDoesNotExistException("None of the supplied textbook ids exist");
		} else {
			return textbookGraphDao.getUsersWhoOwnTextbooks(existingTextbooks);
		}
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooksFromWishList(String userId) throws UserDoesNotExistException {
		boolean userExists = textbookGraphDao.doesUserExist(userId);
		if (userExists) {
			return textbookGraphDao.getUsersWhoOwnWantedTextbooks(userId);
		} else {
			throw new UserDoesNotExistException(String.format("User with id %s doesnt exist", userId));
		}
	}

	public void transferBook(String owner,
							 String consumer,
							 String textbookId)
			throws UserDoesNotExistException, TextbookDoesNotExistException, RelationshipException, UserDoesntOwnTextbookException {
		validateInputsForTransfer(owner, consumer, textbookId);
		Relationship relationship = textbookGraphDao.getTextbookRelationship(owner, OWNS_VERB, textbookId);
		if (null == relationship) {
			throw new UserDoesntOwnTextbookException(String.format("Unable to complete transfer. User %s does not own textbook %s", owner, textbookId));
		}
		boolean deleteOwnsRelationship = textbookGraphDao.deleteTextbookRelationship(owner, OWNS_VERB, textbookId);
		boolean deleteWantsRelationship = textbookGraphDao.deleteTextbookRelationship(consumer, WANTS_VERB, textbookId);
		boolean textbookRelationship = textbookGraphDao.createTextbookRelationship(consumer, OWNS_VERB, textbookId);
		if (!textbookRelationship) {
			throw new RelationshipException(String.format("Unable to complete tranfer between owner %s, consumer %s and textbook %s", owner, consumer, textbookId));
		}

	}

	public List<Textbook> getTextbooksByRelationship(String user, String verb) throws UserDoesNotExistException, VerbException {
		validateInputsForUserAndVerb(user, verb);
		return textbookGraphDao.getTextbooksByVerb(user, verb);
	}

	private void validateInputsForUserAndVerb(String user, String verb) throws VerbException, UserDoesNotExistException {
		if (!textbookGraphDao.doesUserExist(user)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", user));
		}
		if (!textbookGraphDao.isVerbValid(verb)) {
			throw new VerbException(String.format("Verb %s is not valid", verb));
		}
	}

	private void validateInputsForTransfer(String owner, String consumer, String textbookId) throws TextbookDoesNotExistException, UserDoesNotExistException {
		if (!textbookGraphDao.doesUserExist(owner)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", owner));
		}
		if (!textbookGraphDao.doesUserExist(consumer)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", consumer));
		}
		if (!textbookGraphDao.doesTextbookExistById(textbookId)) {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesnt exist", textbookId));
		}
	}

	private void validateInputsForRelationship(String userId, String verb, String textbookId) throws UserDoesNotExistException, VerbException, TextbookDoesNotExistException {
		if (!textbookGraphDao.doesUserExist(userId)) {
			throw new UserDoesNotExistException(String.format("User with id %s doesn't exist", userId));
		}
		if (!textbookGraphDao.isVerbValid(verb)) {
			throw new VerbException(String.format("Verb %s is not valid", verb));
		}
		if (!textbookGraphDao.doesTextbookExistById(textbookId)) {
			throw new TextbookDoesNotExistException(String.format("Textbook with id %s doesnt exist", textbookId));
		}
	}

}
