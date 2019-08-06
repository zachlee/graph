package com.strade.service;

import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import io.javalin.Context;

import java.io.IOException;

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

	public boolean addTextbook(Textbook textbook) throws Exception {
		String textbookId = textbook.getId();
		boolean textbookExists = graphDao.doesTextbookExistById(textbookId);
		if (textbookExists){
			throw new Exception("Textbook Already Exists");
		} else {
			return graphDao.insertTextbook(textbook);
		}
	}

	public void removeTextbook(String textbookId) {
		boolean doesTextbookExist = graphDao.doesTextbookExistById(textbookId);
		if (doesTextbookExist) {
			graphDao.deleteTextbook(textbookId);
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
			return graphDao.removeTextbookRelationship(userId, verb, textbookId);
		} else {
			return false;
		}
	}

	public void searchBook(Context context){}

	public void transferBook(Context context){}

	public void searchWishList(Context context){}

	public boolean addUser(User user) throws IOException {
		return graphDao.addUser(user);
	}

	public boolean removeUser(String userId){
		return graphDao.removeUser(userId);
	}
}
