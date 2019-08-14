package com.strade.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.dao.GraphDao;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import com.strade.service.GraphService;
import io.javalin.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.api.client.http.HttpStatusCodes.*;


public class GraphResource {

	Logger logger = Logger.getLogger(GraphResource.class.getName());
	private static ObjectMapper mapper = new ObjectMapper();
	private static GraphService graphService = GraphService.getInstance();

	//injector for testing
	public GraphResource(GraphService service) {
		graphService = service;
	}

	public static void aboutPage(Context context) {
		graphService.aboutPage(context);
	}

	public static void createTextbook(Context context) throws Exception {
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

	public static void deleteTextbook(Context context) {
		String textbookIdString = context.queryParam("textbook");
		boolean removed = graphService.removeTextbook(textbookIdString);
		context.status(STATUS_CODE_NO_CONTENT);

	}

	public static void createTextbookRelationship(Context context) {
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

	public static void deleteTextbookRelationship(Context context) {
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

	public static void addUser(Context context) throws Exception {
		User user = mapper.readValue(context.body(), User.class);
		boolean userAdded = graphService.addUser(user);
		if (userAdded) {
			context.status(STATUS_CODE_CREATED);
		} else {
			context.status(STATUS_CODE_BAD_REQUEST);
		}
	}

	public static void deleteUser(Context context){
		String userId = context.queryParam("user");
		boolean userRemoved = graphService.removeUser(userId);
		context.status(STATUS_CODE_NO_CONTENT);
	}

	public static void getUser(Context context){
		String userId = context.queryParam("user");
		User user = graphService.getUser(userId);
		context.json(user);
		context.status(STATUS_CODE_OK);
	}

	public static void findUsersWithTextbook(Context context){
		String textbookId = context.queryParam("textbook");
		List<User> usersWithTextbook = graphService.findUsersWithTextbook(textbookId);
		context.json(usersWithTextbook);
		context.status(STATUS_CODE_OK);
	}

	public static void getUsersWhoOwnTextbooks(Context context){
		//todo construct post body
		List<String> textbookIds = new ArrayList<>();
		Map<Long, List<User>> usersWhoOwnTextbooks = graphService.getUsersWhoOwnTextbooks(textbookIds);
		context.json(usersWhoOwnTextbooks);
		context.status(STATUS_CODE_OK);

	}

	public static void searchWishList(Context context) {
		String userId = context.pathParam("user");
		Map<Long, List<User>> usersWhoOwnTextbooksFromWishList = graphService.getUsersWhoOwnTextbooksFromWishList(userId);
		context.json(usersWhoOwnTextbooksFromWishList);
		context.status(STATUS_CODE_OK);
	}

	public static void transferBook(Context context){
		String owningUser = context.pathParam("user");
		String consumingUser = context.pathParam("consumer");
		String textbookId = context.pathParam("textbook");
		graphService.transferBook(owningUser, consumingUser, textbookId);
	}
}
