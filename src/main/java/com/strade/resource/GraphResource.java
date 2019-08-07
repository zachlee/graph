package com.strade.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import com.strade.service.GraphService;
import io.javalin.Context;

import java.io.IOException;

import static com.google.api.client.http.HttpStatusCodes.*;


public class GraphResource {

	private static ObjectMapper mapper = new ObjectMapper();
	private static GraphService graphService = GraphService.getInstance();

	//injector for testing
	public GraphResource(GraphService service) {
		graphService = service;
	}

	public static void aboutPage(Context context) {
		graphService.aboutPage(context);
	}

	public static void addTextbook(Context context) throws Exception {
		Textbook textbook = mapper.readValue(context.body(), Textbook.class);
		boolean inserted = graphService.addTextbook(textbook);
		if ( inserted ) {
			context.status(STATUS_CODE_CREATED);
		} else {
			context.status(STATUS_CODE_SERVER_ERROR);
		}
	}

	public static void getTextbookById(Context context) {
		String textbookId = context.queryParam("textbook");
		Textbook textbook = graphService.getTextbookById(textbookId);
		if (null != textbook) {
			context.json(textbook);
			context.status(STATUS_CODE_OK);
		} else {
			context.status(STATUS_CODE_NOT_FOUND);
		}
	}

	public static void removeTextbook(Context context) {
		String textbookIdString = context.queryParam("textbook");
		boolean removed = graphService.removeTextbook(textbookIdString);
		context.status(STATUS_CODE_NO_CONTENT);

	}

	public static void addTextbookRelationship(Context context) {
		String userId = context.queryParam("user");
		String verb = context.queryParam("verb");
		String textbookId = context.queryParam("textbook");
		boolean relationshipAdded = graphService.addTextbookRelationship(userId, verb, textbookId);
		if (relationshipAdded) {
			context.status(STATUS_CODE_CREATED);
		} else {
			context.status(STATUS_CODE_BAD_REQUEST);
		}
	}

	public static void removeTextbookRelationship(Context context) {
		String userId = context.queryParam("user");
		String verb = context.queryParam("verb");
		String textbookId = context.queryParam("textbook");
		boolean relationshipDeleted = graphService.removeTextbookRelationship(userId, verb, textbookId);
		context.status(STATUS_CODE_NO_CONTENT);
	}

	public static void getTextbookRelationship(Context context){
		String userId = context.queryParam("user");
		String verb = context.queryParam("verb");
		String textbookId = context.queryParam("textbook");
		Relationship relationship = graphService.getTextbookRelationship(userId, verb, textbookId);
		context.json(relationship);
		context.status(STATUS_CODE_OK);
	}

	public static void searchBook(Context context){}

	public static void transferBook(Context context){}

	public static void searchWishlist(Context context){}

	public static void addUser(Context context) throws IOException {
		User user = mapper.readValue(context.body(), User.class);
		boolean userAdded = graphService.addUser(user);
		if (userAdded) {
			context.status(STATUS_CODE_CREATED);
		} else {
			context.status(STATUS_CODE_BAD_REQUEST);
		}
	}

	public static void removeUser(Context context){
		String userId = context.queryParam("user");
		boolean userRemoved = graphService.removeUser(userId);
		context.status(STATUS_CODE_NO_CONTENT);
	}
}
