package com.studentrade.graph.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.studentrade.graph.domain.Relationship;
import com.studentrade.graph.domain.Textbook;
import com.studentrade.graph.domain.User;
import com.studentrade.graph.domain.request.GetUsersWithTextbookRequest;
import com.studentrade.graph.exception.*;
import com.studentrade.graph.service.GraphService;
import io.javalin.http.Context;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.api.client.http.HttpStatusCodes.*;
import static com.google.api.client.http.HttpStatusCodes.STATUS_CODE_BAD_REQUEST;

@Singleton
public class GraphResource {

	private final GraphService graphService;
	private final ObjectMapper mapper;

	@Inject
	public GraphResource(GraphService graphService) {
		this.graphService = graphService;
		this.mapper = new ObjectMapper();
	}

	public void healthcheck(Context context) {
		context.status(STATUS_CODE_OK)
				.json("bookshelf-graph");
	}

	public void createTextbook(Context context) {
		try {
			Textbook textbook = mapper.readValue(context.body(), Textbook.class);
			graphService.addTextbook(textbook);
			context.status(STATUS_CODE_CREATED);
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST);
		} catch (TextbookException e) {
			if (e instanceof TextbookAlreadyExistsException) {
				context.status(STATUS_CODE_BAD_REQUEST);
			} else {
				context.status(STATUS_CODE_SERVER_ERROR);
			}
			context.json(e.getMessage());
		}
	}

	public void getTextbookById(Context context) {
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(textbookId);
			Textbook textbook = graphService.getTextbookById(textbookId);
			context.status(STATUS_CODE_OK);
			context.json(textbook);
		} catch (TextbookDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (TextbookException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public void deleteTextbook(Context context) {
		String textbookIdString = context.pathParam("textbook");
		graphService.removeTextbook(textbookIdString);
		context.status(STATUS_CODE_NO_CONTENT);
	}

	public void createTextbookRelationship(Context context) {
		String userId = context.pathParam("user");
		String verb = context.pathParam("verb");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(userId, verb, textbookId);
			graphService.addTextbookRelationship(userId, verb, textbookId);
			context.status(STATUS_CODE_CREATED);
		} catch (TextbookDoesNotExistException | UserDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (VerbException | IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public void deleteTextbookRelationship(Context context) {
		String userId = context.pathParam("user");
		String verb = context.pathParam("verb");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(userId, verb, textbookId);
			graphService.removeTextbookRelationship(userId, verb, textbookId);
			context.status(STATUS_CODE_NO_CONTENT);
		} catch (TextbookDoesNotExistException | UserDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (VerbException | IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public void getTextbookRelationship(Context context) {
		String userId = context.pathParam("user");
		String verb = context.pathParam("verb");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(userId, verb, textbookId);
			Relationship relationship = graphService.getTextbookRelationship(userId, verb, textbookId);
			context.json(relationship)
					.status(STATUS_CODE_OK);
		} catch (TextbookDoesNotExistException | UserDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (VerbException | IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public void addUser(Context context) {
		try {
			User user = mapper.readValue(context.body(), User.class);
			graphService.addUser(user);
			context.status(STATUS_CODE_CREATED);
		} catch (IOException | UserAlreadyExistsException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (UserException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public void deleteUser(Context context) {
		String userId = context.pathParam("user");
		try {
			validateInputs(userId);
			graphService.removeUser(userId);
			context.status(STATUS_CODE_NO_CONTENT);
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public void getUser(Context context) {
		String userId = context.pathParam("user");
		try {
			validateInputs(userId);
			User user = graphService.getUser(userId);
			context.status(STATUS_CODE_OK)
					.json(user);
		} catch (UserDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public void findUsersWithTextbook(Context context) {
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(textbookId);
			List<User> usersWithTextbook = graphService.findUsersWithTextbook(textbookId);
			context.status(STATUS_CODE_OK)
					.json(usersWithTextbook);
		} catch (TextbookDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public void getUsersWhoOwnTextbooks(Context context){
		try {
			GetUsersWithTextbookRequest request = mapper.readValue(context.body(), GetUsersWithTextbookRequest.class);
			List<String> textbookIds = request.getTextbooks();
			Map<Long, List<User>> usersWhoOwnTextbooks = graphService.getUsersWhoOwnTextbooks(textbookIds);
			context.status(STATUS_CODE_OK)
					.json(usersWhoOwnTextbooks);
		} catch (TextbookDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public void searchWishList(Context context) {
		String userId = context.pathParam("user");
		try {
			validateInputs(userId);
			Map<Long, List<User>> usersWhoOwnTextbooksFromWishList = graphService.getUsersWhoOwnTextbooksFromWishList(userId);
			context.status(STATUS_CODE_OK)
					.json(usersWhoOwnTextbooksFromWishList);
		} catch (UserDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (IOException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		}
	}

	public void transferBook(Context context) {
		String owningUser = context.pathParam("user");
		String consumingUser = context.pathParam("consumer");
		String textbookId = context.pathParam("textbook");
		try {
			validateInputs(owningUser, consumingUser, textbookId);
			graphService.transferBook(owningUser, consumingUser, textbookId);
			context.status(STATUS_CODE_CREATED);
		} catch (UserDoesNotExistException | TextbookDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (IOException | UserDoesntOwnTextbookException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
					.json(e.getMessage());
		} catch (RelationshipException e) {
			context.status(STATUS_CODE_SERVER_ERROR)
					.json(e.getMessage());
		}
	}

	public void getAllRelationshipsByVerb(Context context) {
		String user = context.pathParam("user");
		String verb = context.pathParam("verb");
		try {
			List<Textbook> textbookList = graphService.getTextbooksByRelationship(user, verb);
			context.status(STATUS_CODE_OK)
					.json(textbookList);
		} catch (UserDoesNotExistException e) {
			context.status(STATUS_CODE_NOT_FOUND)
					.json(e.getMessage());
		} catch (VerbException e) {
			context.status(STATUS_CODE_BAD_REQUEST)
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