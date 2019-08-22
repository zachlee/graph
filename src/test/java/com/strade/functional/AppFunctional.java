package com.strade.functional;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jayway.restassured.RestAssured.given;
import static com.strade.util.TestUtils.*;
import static com.strade.utils.Labels.OWNS_VERB;
import static com.strade.utils.Labels.WANTS_VERB;

public class AppFunctional {
	private Logger logger = Logger.getLogger(AppFunctional.class.getName());
	private String host = "http://localhost:7000";

	public AppFunctional() {
	}

	private static GraphTraversalSource graphTraversalSource;

	@BeforeClass
	public static void setup() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	@Test
	public void aboutPage() throws IOException {
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.get(host + "/about");
		response.then().log().everything().and().assertThat().contentType(ContentType.JSON);
		String as = response.getBody().as(String.class);
		logger.log(Level.INFO, as);
	}

	@Test
	public void getUser() {
		String userId = UUID.randomUUID().toString();
		User user = createUser(userId);
		createUserTraversalAndAssert(user, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.with()
					.pathParameter("user", userId)
					.get(host + "/graph/internal/users/{user}");
			response.then().log().everything();
			User returnedUser = response.getBody().as(User.class);
			assert null != returnedUser;
			assert response.statusCode() == 200;
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getNonExistentUserReturns404() {
		String nonExistentUser = UUID.randomUUID().toString();
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.with()
				.pathParameter("user", nonExistentUser)
				.get(host + "/graph/internal/users/{user}");
		response.then().log().everything();
		logger.log(Level.SEVERE, response.getBody().print());
		assert response.statusCode() == 404;
	}

	@Test
	public void createUserReturns201() {
		String userId = UUID.randomUUID().toString();
		User user = createUser(userId);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.body(user)
					.post(host + "/graph/internal/users/{user}");
			response.then().log().everything();
			assert response.statusCode() == 201;
			assert doesUserExist(userId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void createUserThatAlreadyExistsThrows400() {
		String userId = UUID.randomUUID().toString();
		User user = createUser(userId);
		createUserTraversalAndAssert(user, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.body(user)
					.post(host + "/graph/internal/users/{user}");
			response.then().log().everything();
			logger.log(Level.SEVERE, response.getBody().print());
			assert response.statusCode() == 400;
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void createUserInvalidBodyThrows400() {
		String userId = UUID.randomUUID().toString();
		String invalidBody = "invalidBody";
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.body(invalidBody)
				.pathParameter("user", userId)
				.post(host + "/graph/internal/users/{user}");
		response.then().log().everything();
		logger.log(Level.INFO, String.valueOf(response.getStatusCode()));
		assert response.statusCode() == 400;
	}

	@Test
	public void deleteUserReturns204() {
		String userId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.pathParameter("user", userId)
				.delete(host + "/graph/internal/users/{user}");
		response.then().log().everything();
		assert !doesUserExist(userId, graphTraversalSource);
		assert response.statusCode() == 204;
	}

	@Test
	public void deleteNonExistingUserReturns204() {
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.pathParameter("user", "doesntExist")
				.delete(host + "/graph/internal/users/{user}");
		response.then().log().everything();
		assert response.statusCode() == 204;
	}

	@Test
	public void createTextbookReturns201() {
		String textbookId = UUID.randomUUID().toString();
		Textbook textbook = createTextbookObject(textbookId);
		try {
			assert !doesTextbookExist(textbookId, graphTraversalSource);
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.body(textbook)
					.pathParameter("textbook", textbookId)
					.post(host + "/graph/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 201;
			assert doesTextbookExist(textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void createTextbookBadPostBodyThrows400() {
		String textbookId = UUID.randomUUID().toString();
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.body("invalidBody")
				.pathParameter("textbook", textbookId)
				.post(host + "/graph/textbooks/{textbook}");
		response.then().log().everything();
		assert response.statusCode() == 400;
	}

	@Test
	public void createTextbookThatAlreadyExistsThrows400() {
		String textbookId = UUID.randomUUID().toString();
		Textbook textbook = createTextbookObject(textbookId);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.body(textbook)
					.pathParameter("textbook", textbookId)
					.post(host + "/graph/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 400;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getTextbookByIdReturns200() {
		String textbookId = UUID.randomUUID().toString();
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("textbook", textbookId)
					.get(host + "/graph/textbooks/{textbook}");
			response.then().log().everything();
			Textbook returnedTextbook = response.getBody().as(Textbook.class);
			assert response.statusCode() == 200;
			assert returnedTextbook.getUuid().equals(textbookId);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getTextbookThatDoesntExistReturns404() {
		String textbookId = UUID.randomUUID().toString();
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.pathParameter("textbook", textbookId)
				.get(host + "/graph/textbooks/{textbook}");
		response.then().log().everything();
		assert response.statusCode() == 404;
	}

	@Test
	public void deleteTextbookReturns204() {
		String textbookId = UUID.randomUUID().toString();
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		assert doesTextbookExist(textbookId, graphTraversalSource);
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.pathParameter("textbook", textbookId)
				.delete(host + "/graph/textbooks/{textbook}");
		response.then().log().everything();
		assert response.statusCode() == 204;
		assert !doesTextbookExist(textbookId, graphTraversalSource);
	}

	@Test
	public void createTextbookRelationshipReturns201() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.post(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 201;
			assert getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void createRelationshipTextbookDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.post(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 404;
			assert !getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void createRelationshihpUserDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.post(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 404;
			assert !getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void deleteRelationshipReturns204() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
		try {
			assert getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.delete(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 204;
			assert !getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void deleteRelationshipTextbookDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		try {
			assert !getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.delete(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 404;
			assert !getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void deleteRelationshipUserDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		try {
			assert !getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.delete(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 404;
			assert !getUserTextbookRelationshipExists(userId, WANTS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getTextbookRelationshipReturns200() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.get(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			Relationship relationship = response.getBody().as(Relationship.class);
			assert response.statusCode() == 200;
			assert relationship.getUser().equals(userId);
			assert relationship.getTextbook().equals(textbookId);
			assert relationship.getVerb().equals(WANTS_VERB);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getTextbookRelationshipTextbookDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.get(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 404;
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getTextbookRelationshipUserDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.pathParameter("verb", WANTS_VERB)
					.pathParameter("textbook", textbookId)
					.get(host + "/graph/users/{user}/verbs/{verb}/textbooks/{textbook}");
			response.then().log().everything();
			assert response.statusCode() == 404;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void findUsersWithTextbookReturns200() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId2, textbookId, OWNS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("textbook", textbookId)
					.get(host + "/graph/textbooks/{textbook}/users");
			response.then().log().everything();
			User[] users = response.getBody().as(User[].class);
			assert users.length == 2;
			assert response.statusCode() == 200;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
		}
	}

	@Test
	public void findUsersWithTextbookTextbookDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("textbook", textbookId)
					.get(host + "/graph/textbooks/{textbook}/users");
			response.then().log().everything();
			assert response.statusCode() == 404;
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
		}
	}

	@Test
	public void findUsersWithTextbookReturnsEmptyList() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("textbook", textbookId)
					.get(host + "/graph/textbooks/{textbook}/users");
			response.then().log().everything();
			User[] users = response.getBody().as(User[].class);
			assert users.length == 0;
			assert response.statusCode() == 200;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
		}
	}

	@Test
	public void searchWishListReturns200() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String textbookId2 = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId3), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId2, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId2, WANTS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId2, textbookId, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId3, textbookId, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId3, textbookId2, OWNS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.get(host + "/graph/users/{user}/wishlist");
			response.then().log().everything();
			Map map = response.getBody().as(Map.class);
			assert map.size() == 2;
			assert response.statusCode() == 200;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId2).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
			graphTraversalSource.V().has("uuid", userId3).drop().iterate();
		}
	}

	@Test
	public void searchWishListUserDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String textbookId2 = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId3), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId2, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId2, textbookId, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId3, textbookId, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId3, textbookId2, OWNS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.get(host + "/graph/users/{user}/wishlist");
			response.then().log().everything();
			assert response.statusCode() == 404;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId2).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
			graphTraversalSource.V().has("uuid", userId3).drop().iterate();
		}
	}

	@Test
	public void searchWishListReturnsEmptyList() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId)
					.get(host + "/graph/users/{user}/wishlist");
			response.then().log().everything();
			Map map = response.getBody().as(Map.class);
			assert map.size() == 0;
			assert response.statusCode() == 200;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void transferBookReturns201() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId2, textbookId, OWNS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId2)
					.pathParam("textbook", textbookId)
					.pathParam("consumer", userId)
					.post(host + "/graph/users/{user}/textbooks/{textbook}/users/{consumer}/transfer");
			response.then().log().everything();
			assert response.getStatusCode() == 201;
			assert getUserTextbookRelationshipExists(userId, OWNS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
		}
	}

	@Test
	public void transferBookUserDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId2)
					.pathParam("textbook", textbookId)
					.pathParam("consumer", userId)
					.post(host + "/graph/users/{user}/textbooks/{textbook}/users/{consumer}/transfer");
			response.then().log().everything();
			assert response.getStatusCode() == 404;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void transferBookTextbookDoesntExistReturns404() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId2)
					.pathParam("textbook", textbookId)
					.pathParam("consumer", userId)
					.post(host + "/graph/users/{user}/textbooks/{textbook}/users/{consumer}/transfer");
			response.then().log().everything();
			assert response.getStatusCode() == 404;
			assert !getUserTextbookRelationshipExists(userId, OWNS_VERB, textbookId, graphTraversalSource);
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
		}
	}

	@Test
	public void transferBookOwnerDoesntHaveOwningRelationship() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.pathParameter("user", userId2)
					.pathParam("textbook", textbookId)
					.pathParam("consumer", userId)
					.post(host + "/graph/users/{user}/textbooks/{textbook}/users/{consumer}/transfer");
			response.then().log().everything();
			assert response.getStatusCode() == 400;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksReturns200() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String userId4 = UUID.randomUUID().toString();

		String textbookId = "textbook";
		String textbookId2 = "textbook2";
		String textbookId3 = "textbook3";
		String textbookId4 = "textbook4";

		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId3), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId4), graphTraversalSource);

		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId2, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId3, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId4, "isbn10", "isbn13", graphTraversalSource);

		createRelationshipAndAssert(userId, textbookId, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId2, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId3, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId, textbookId4, OWNS_VERB, graphTraversalSource);
		createRelationshipAndAssert(userId2, textbookId2, OWNS_VERB, graphTraversalSource);

		String body = "{ \"textbooks\": [\"textbook1\", \"textbook2\", \"textbook3\", \"textbook4\"] }";
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.body(body)
					.post(host + "/graph/textbooks");
			response.then().log().everything();
			assert response.getStatusCode() == 200;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId2).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId3).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId4).drop().iterate();

			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
			graphTraversalSource.V().has("uuid", userId3).drop().iterate();
			graphTraversalSource.V().has("uuid", userId4).drop().iterate();
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksNoTextbooksFoundReturns404() {
		String userId = UUID.randomUUID().toString();
		String body = "{ \"textbooks\": [\"textbook1\", \"textbook2\", \"textbook3\", \"textbook4\"] }";
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.body(body)
					.post(host + "/graph/textbooks");
			response.then().log().everything();
			assert response.getStatusCode() == 404;
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getUsersWhoOwnTextbooksNoUsersFoundReturns200() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String userId4 = UUID.randomUUID().toString();

		String textbookId = "textbook";
		String textbookId2 = "textbook2";
		String textbookId3 = "textbook3";
		String textbookId4 = "textbook4";

		createUserTraversalAndAssert(createUser(userId), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId2), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId3), graphTraversalSource);
		createUserTraversalAndAssert(createUser(userId4), graphTraversalSource);

		createTextbookTraversalAndAssert(textbookId, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId2, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId3, "isbn10", "isbn13", graphTraversalSource);
		createTextbookTraversalAndAssert(textbookId4, "isbn10", "isbn13", graphTraversalSource);

		String body = "{ \"textbooks\": [\"textbook1\", \"textbook2\", \"textbook3\", \"textbook4\"] }";
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.body(body)
					.post(host + "/graph/textbooks");
			response.then().log().everything();
			assert response.getStatusCode() == 200;
		} finally {
			graphTraversalSource.V().has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId2).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId3).drop().iterate();
			graphTraversalSource.V().has("uuid", textbookId4).drop().iterate();

			graphTraversalSource.V().has("uuid", userId).drop().iterate();
			graphTraversalSource.V().has("uuid", userId2).drop().iterate();
			graphTraversalSource.V().has("uuid", userId3).drop().iterate();
			graphTraversalSource.V().has("uuid", userId4).drop().iterate();
		}
	}
}
