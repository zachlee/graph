package com.strade.resource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import com.strade.exceptions.*;
import com.strade.service.GraphService;
import com.sun.javaws.exceptions.InvalidArgumentException;
import io.javalin.Context;

import java.io.IOException;
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

	public static void aboutPage(Context context) throws IOException {
		String aboutReturn = graphService.aboutPage();
		context.contentType("application/json")
				.status(STATUS_CODE_OK)
				.json(aboutReturn);
	}

	public static void createTextbook(Context context) {
		try {
			Textbook textbook = mapper.readValue(context.body(), Textbook.class);
			graphService.addTextbook(textbook);
			context.status(STATUS_CODE_OK);
		} catch (IOException e) {
			context.status(STATUS_CODE_SERVER_ERROR);
		} catch (TextbookException e) {
			if (e instanceof TextbookAlreadyExistsException) {
				context.status(STATUS_CODE_BAD_REQUEST);
			} else {
				context.status(STATUS_CODE_SERVER_ERROR);
			}
			context.json(e.getMessage());
		}
	}

	public static void getTextbookById(Context context) {
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(textbookId);
			Textbook textbook = graphService.getTextbookById(textbookId);
			context.status(STATUS_CODE_OK);
			context.json(textbook);
		} catch (TextbookException e) {
			context.status(STATUS_CODE_SERVER_ERROR);
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST);
		}
	}

	public static void deleteTextbook(Context context) {
		String textbookIdString = context.pathParam("textbook");
		graphService.removeTextbook(textbookIdString);
		context.status(STATUS_CODE_NO_CONTENT);
	}

	public static void createTextbookRelationship(Context context) {
		String userId = context.pathParam("user");
		String verb = context.pathParam("verb");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(userId, verb, textbookId);
			graphService.addTextbookRelationship(userId, verb, textbookId);
			context.status(STATUS_CODE_CREATED);
		} catch (VerbException | IOException | TextbookDoesNotExistException | UserDoesNotExistException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void deleteTextbookRelationship(Context context) {
		String userId = context.pathParam("user");
		String verb = context.pathParam("verb");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(userId, verb, textbookId);
			graphService.removeTextbookRelationship(userId, verb, textbookId);
			context.status(STATUS_CODE_NO_CONTENT);
		} catch (VerbException | IOException | TextbookDoesNotExistException | UserDoesNotExistException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void getTextbookRelationship(Context context) {
		String userId = context.pathParam("user");
		String verb = context.pathParam("verb");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(userId, verb, textbookId);
			Relationship relationship = graphService.getTextbookRelationship(userId, verb, textbookId);
			context.json(relationship)
					.status(STATUS_CODE_OK);
		} catch (VerbException | IOException | TextbookDoesNotExistException | UserDoesNotExistException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void addUser(Context context) {
		try {
			User user = mapper.readValue(context.body(), User.class);
			graphService.addUser(user);
			context.status(STATUS_CODE_CREATED);
		} catch (UserAlreadyExistsException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (IOException | UserException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void deleteUser(Context context) {
		String userId = context.pathParam("user");
		try {
			validateInputs(userId);
			graphService.removeUser(userId);
			context.status(STATUS_CODE_NO_CONTENT);
		} catch (IOException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void getUser(Context context) {
		String userId = context.pathParam("user");
		try {
			validateInputs(userId);
			User user = graphService.getUser(userId);
			context.status(STATUS_CODE_OK)
					.json(user);
		} catch (UserDoesNotExistException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (IOException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void findUsersWithTextbook(Context context) {
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(textbookId);
			List<User> usersWithTextbook = graphService.findUsersWithTextbook(textbookId);
			context.status(STATUS_CODE_OK)
					.json(usersWithTextbook);
		} catch (TextbookDoesNotExistException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (IOException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void getUsersWhoOwnTextbooks(Context context) {
		//todo construct post body
		List<String> textbookIds = new ArrayList<>();
		try {
			Map<Long, List<User>> usersWhoOwnTextbooks = graphService.getUsersWhoOwnTextbooks(textbookIds);
			context.status(STATUS_CODE_OK)
					.json(usersWhoOwnTextbooks);
		} catch (TextbookDoesNotExistException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public static void searchWishList(Context context) {
		String userId = context.pathParam("user");
		try {
			validateInputs(userId);
			Map<Long, List<User>> usersWhoOwnTextbooksFromWishList = graphService.getUsersWhoOwnTextbooksFromWishList(userId);
			context.status(STATUS_CODE_OK)
					.json(usersWhoOwnTextbooksFromWishList);
		} catch (UserDoesNotExistException | IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public static void transferBook(Context context) {
		String owningUser = context.pathParam("user");
		String consumingUser = context.pathParam("consumer");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(owningUser, consumingUser, textbookId);
			graphService.transferBook(owningUser, consumingUser, textbookId);
			context.status(STATUS_CODE_CREATED);
		} catch (UserDoesNotExistException | IOException | TextbookDoesNotExistException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public static void validateInputs(String... inputs) throws IOException {
		for (String input : inputs) {
			if (null == input) {
				throw new IOException("One or more of the path parameters were null");
			}
		}
	}
}
