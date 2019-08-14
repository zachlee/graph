package com.strade.service;

import com.strade.dao.GraphDao;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import io.javalin.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphService {

	private static GraphDao graphDao = GraphDao.getInstance();
	private static GraphService instance;

	GraphService(GraphDao graphDao) {
		this.graphDao = graphDao;
	}

	public static GraphService getInstance() {
		if (instance == null)
			instance = new GraphService(graphDao);
		return instance;
	}

	public  void aboutPage( Context ctx ) { ctx.result( "studentrade-graph" ); }

	public boolean addUser(User user) throws Exception {
		String userId = user.getUuid();
		boolean userExists = graphDao.doesUserExist(userId);
		//todo real exception
		if (userExists) {
			throw new Exception("User Already Exists");
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
		//todo real exception
		if (textbookExists){
			throw new Exception("Textbook Already Exists");
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

	public Textbook getTextbookById(String textbookId) {
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if (doesTextbookExist){
			return graphDao.getTextbook(textbookId);
		} else {
			return null;
		}
	}

	public boolean addTextbookRelationship(String userId, String verb, String textbookId){
		boolean doesUserExist = graphDao.doesUserExist(userId);
		boolean verbValid = graphDao.isVerbValid(verb);
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if (doesUserExist && verbValid && doesTextbookExist) {
			return graphDao.createTextbookRelationship(userId, verb, textbookId);
		} else {
			return false;
		}
	}

	public boolean removeTextbookRelationship(String userId, String verb, String textbookId) {
		boolean doesUserExist = graphDao.doesUserExist(userId);
		boolean verbValid = graphDao.isVerbValid(verb);
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if ( doesUserExist && verbValid && doesTextbookExist) {
			return graphDao.deleteTextbookRelationship(userId, verb, textbookId);
		} else {
			return false;
		}
	}

	public Relationship getTextbookRelationship(String userId, String verb, String textbookId) {
		boolean doesUserExist = graphDao.doesUserExist(userId);
		boolean verbValid = graphDao.isVerbValid(verb);
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if ( doesUserExist && verbValid && doesTextbookExist) {
			return graphDao.getTextbookRelationship(userId, verb, textbookId);
		} else {
			return null;
		}
	}

	public List<User> findUsersWithTextbook(String textbookId){
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if (doesTextbookExist) {
			return graphDao.getUsersWhoOwnTextbook(textbookId);
		} else {
			return null;
		}
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooks(List<String> textbookIds) {
		List<String> existingTextbooks = new ArrayList<>();
		for ( String textbookId : textbookIds ) {
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
}
