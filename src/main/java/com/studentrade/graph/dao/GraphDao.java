package com.studentrade.graph.dao;

import com.studentrade.graph.domain.Relationship;
import com.studentrade.graph.domain.Textbook;
import com.studentrade.graph.domain.User;

import java.util.List;
import java.util.Map;

public interface GraphDao {
	boolean doesTextbookExistById(String textbookId);
	boolean doesTextbookExistByIsbn10(String isbn10);
	boolean doesTextbookExistByIsbn13(String isbn13);
	boolean doesTextbookExist(String textbookId, String isbn10, String isbn13);

	boolean isVerbValid(String verb);

	Textbook getTextbook(String textbookId);
	Textbook getTextbookIsbn10(String isbn10);
	Textbook getTextbookIsbn13(String isbn13);
	List<Textbook> getTextbooksByVerb(String userId, String verb);


	boolean createTextbook(Textbook textbook);
	boolean deleteTextbook(String textbookId);

	boolean createUser(User user);
	boolean doesUserExist(String userId);
	boolean deleteUser(String userId);
	User getUser(String userId);

	boolean createTextbookRelationship(String userId, String verb, String textbookId);
	boolean deleteTextbookRelationship(String userId, String verb, String texbookId);
	Relationship getTextbookRelationship(String userId, String verb, String textbookId);

	List<User> getUsersWhoOwnTextbook(String textbookId);
	Map<Long, List<User>> getUsersWhoOwnTextbooks(List<String> textbookIds);
	Map<Long, List<User>> getUsersWhoOwnWantedTextbooks(String userId);
}
