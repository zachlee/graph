package com.strade.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.dao.GraphDao;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import com.strade.service.GraphService;
import io.javalin.Context;

import java.io.IOException;

public class GraphResource {

	private static final GraphService graphService = new GraphService();
	private static ObjectMapper mapper = new ObjectMapper();

	public static void aboutPage(Context context) {
		graphService.aboutPage(context);
	}

	public static void addTextbook(Context context) throws Exception {
		Textbook textbook = mapper.readValue(context.body(), Textbook.class);
		boolean inserted = graphService.addTextbook(textbook);
		if ( inserted ) {
			context.status(200);
		} else {
			context.status(500);
		}
	}

	public static void removeTextbook(Context context) {
		String textbookIdString = context.queryParam("textbook");
		graphService.removeTextbook(textbookIdString);
		context.status(204);
	}

	public static void addTextbookRelationship(Context context) {
		String userId = context.queryParam("user");
		String verb = context.queryParam("verb");
		String textbookId = context.queryParam("textbook");
		boolean textbookAdded = graphService.addTextbookRelationship(userId, verb, textbookId);
		if (textbookAdded) {
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
		boolean userAdded = graphService.addUser(user);
		if (userAdded) {
			context.status(201);
		} else {
			context.status(400);
		}
	}

	public static void removeUser(Context context){
		String userId = context.queryParam("user");
		boolean userRemoved = graphService.removeUser(userId);
		if (userRemoved) {
			context.status(200);
		} else {
			context.status(400);
		}
	}
}
