package com.strade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import io.javalin.Context;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.UUID;

public class GraphService {

	private static ObjectMapper mapper = new ObjectMapper();
	static GraphDao graphDao = new GraphDao();

	public static void aboutPage( Context ctx ) { ctx.result( "studentrade-graph" ); }

	public static void addTextbook(Context context) throws IOException {
		Textbook textbook = mapper.readValue(context.body(), Textbook.class);
		boolean inserted = graphDao.insertTextbook(textbook);
		if ( inserted ) {
			context.status(200);
		} else {
			context.status(500);
		}
	}

	public static void removeTextbook(Context context){
		String textbookIdString = context.queryParam("textbook");
		UUID textbookId = UUID.fromString(textbookIdString);
		boolean doesTextbookExist = graphDao.doesTextbookExist(textbookId);
		if ( doesTextbookExist ) {
			boolean removed = graphDao.deleteTextbook(textbookId);
		}
		context.status(204);
	}

	public static void addTextbookRelationship(Context context){
		String textbookId = context.queryParam("textbook");
		String verb = context.queryParam("verb");
		String userId = context.queryParam("user");
		boolean relationshipAdded = graphDao.createTextbookRelationship(userId, verb, textbookId);
		if ( relationshipAdded ) {
			context.status(201);
		} else {
			context.status(400);
		}
	}

	public static void searchBook(Context context){}

	public static void transferBook(Context context){}

	public static void searchWishlist(Context context){}

	public static void addUser(Context context) throws IOException {
		User user = mapper.readValue(context.body(), User.class);
		boolean userAdded = graphDao.addUser( user );
		if ( userAdded ) {
			context.status(201);
		} else {
			context.status(400);
		}
	}

	public static void removeUser(Context context){
		String userId = context.queryParam("user");
		boolean userRemoved = graphDao.removeUser( userId );
		context.status(200);
	}
}
