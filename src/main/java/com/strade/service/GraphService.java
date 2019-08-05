package com.strade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import io.javalin.Context;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

public class GraphService {

	private static GraphDao graphDao;

	GraphService(GraphDao graphDao) {
		this.graphDao = graphDao;
	}

	public GraphService() {
		this.graphDao = GraphDao.getInstance();
	}

	public  void aboutPage( Context ctx ) { ctx.result( "studentrade-graph" ); }

	public boolean addTextbook(Textbook textbook) throws Exception {
		String textbookId = textbook.getId();
		boolean textbookExists = graphDao.doesTextbookExist(textbookId);
		if (textbookExists){
			throw new Exception("Textbook Already Exists");
		} else {
			return graphDao.insertTextbook(textbook);
		}
	}

	public void removeTextbook(String textbookId){
		boolean doesTextbookExist = graphDao.doesTextbookExist(textbookId);
		if (doesTextbookExist) {
			graphDao.deleteTextbook(textbookId);
		}
	}

	public boolean addTextbookRelationship(String userId, String verb, String textbookId){
		boolean doesUserExist = graphDao.doesUserExist(userId);
		boolean verbValid = graphDao.isVerbValid(verb);
		boolean doesTextbookExist = graphDao.doesTextbookExist(textbookId);
		if (doesUserExist && verbValid && doesTextbookExist) {
			return graphDao.createTextbookRelationship(userId, verb, textbookId);
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
