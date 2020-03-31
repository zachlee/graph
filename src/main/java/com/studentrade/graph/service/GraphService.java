package com.studentrade.graph.service;

import com.studentrade.graph.domain.Relationship;
import com.studentrade.graph.domain.Textbook;
import com.studentrade.graph.domain.User;
import com.studentrade.graph.exception.*;

import java.util.List;
import java.util.Map;

public interface GraphService {

	void addUser(User user) throws UserException;
	void removeUser(String userId);
	User getUser(String userId) throws UserDoesNotExistException;

	void addTextbook(Textbook textbook) throws TextbookException;
	void removeTextbook(String textbookId);

	Textbook getTextbookById(String textbookId) throws TextbookException;
	Textbook getTextbookByIsbn10(String isbn10) throws TextbookException;
	Textbook getTextbookByIsbn13(String isbn13) throws TextbookException;

	void addTextbookRelationship(String userId,
								 String verb,
								 String textbookId)
			throws UserDoesNotExistException,
			VerbException,
			TextbookDoesNotExistException,
			RelationshipException;

	void removeTextbookRelationship(String userId,
									String verb,
									String textbookId)
			throws TextbookDoesNotExistException,
			UserDoesNotExistException,
			VerbException,
			RelationshipException;

	Relationship getTextbookRelationship(String userId,
										 String verb,
										 String textbookId)
			throws RelationshipException,
			TextbookDoesNotExistException,
			UserDoesNotExistException,
			VerbException;

	List<User> findUsersWithTextbook(String textbookId) throws TextbookDoesNotExistException;
	Map<Long, List<User>> getUsersWhoOwnTextbooks(List<String> textbookIds) throws TextbookDoesNotExistException;
	Map<Long, List<User>> getUsersWhoOwnTextbooksFromWishList(String userId) throws UserDoesNotExistException;

	void transferBook(String owner,
					  String consumer,
					  String textbookId)
			throws UserDoesNotExistException, TextbookDoesNotExistException, RelationshipException, UserDoesntOwnTextbookException;

	List<Textbook> getTextbooksByRelationship(String user, String verb) throws UserDoesNotExistException, VerbException;
}
