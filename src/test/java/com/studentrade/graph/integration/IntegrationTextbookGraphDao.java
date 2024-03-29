package com.studentrade.graph.integration;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.studentrade.graph.dao.TextbookGraphDao;
import com.studentrade.graph.dao.TextbookGraphDaoImpl;
import com.studentrade.graph.domain.Relationship;
import com.studentrade.graph.domain.Textbook;
import com.studentrade.graph.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.driver.ser.Serializers;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.studentrade.graph.util.TestUtils.*;
import static com.studentrade.graph.util.Labels.*;

public class IntegrationTextbookGraphDao {

	private Logger logger = Logger.getLogger(IntegrationTextbookGraphDao.class.getName());
	private static TextbookGraphDao textbookGraphDao;
	private static GraphTraversalSource graphTraversalSource;

	private static final DynamicStringProperty GREMLIN_DOMAIN = DynamicPropertyFactory.getInstance()
			.getStringProperty("gremlin.server.domain", "");

	@BeforeAll
	public static void setup() throws IOException {
		//todo set up functional.properties flow
		//ConfigurationManager.getConfigInstance().setProperty("gremlin.server.domain", "54.184.253.228");
		Cluster cluster = Cluster.build()
				.port(8182)
				.addContactPoint(GREMLIN_DOMAIN.get())
				.serializer(Serializers.GRAPHSON_V3D0)
				.create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
		textbookGraphDao = new TextbookGraphDaoImpl();
	}

	@Test
	public void createUserTest() {
		String userId = UUID.randomUUID().toString();
		try {
			User user = createUser(userId);
			long startTime = System.currentTimeMillis();
			boolean addUser = textbookGraphDao.createUser(user);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "create new user = " + (endTime - startTime) + "ms");
			assert addUser;
			GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
					.hasLabel(USER_LABEL)
					.has("uuid", userId)
					.limit(1)
					.valueMap(true);
			Map<Object, Object> userMap = getUserTraversal.next();
			User returnedUser = createUserFromMap(userMap);
			assert null != returnedUser;
			assert returnedUser.getUuid().equals(userId);
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getUserReturnsUser() {
		String userId = UUID.randomUUID().toString();
		try {
			User user = createUser(userId);
			createUserTraversalAndAssert(user, graphTraversalSource);
			long startTime = System.currentTimeMillis();
			User daoUser = textbookGraphDao.getUser(userId);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "get existing user = " + (endTime - startTime) + "ms");
			assert null != daoUser;
			assert daoUser.getUuid().equals(userId);
			assert daoUser.getType().equals(user.getType());
			assert daoUser.getUsername().equals(user.getUsername());
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getNonExistingUserReturnsNull() {
		long startTime = System.currentTimeMillis();
		User nonExistentUser = textbookGraphDao.getUser("DoesntExist");
		long endTime = System.currentTimeMillis();
		logger.log(Level.INFO, "get nonexisting user = " + (endTime - startTime) + "ms");
		assert null == nonExistentUser;
	}

	@Test
	public void deleteUserTest() {
		String userId = UUID.randomUUID().toString();
		User user = createUser(userId);
		createUserTraversalAndAssert(user, graphTraversalSource);
		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		Map<Object, Object> userMap = getUserTraversal.next();
		User getUser = createUserFromMap(userMap);

		assert null != getUser;

		long startTime = System.currentTimeMillis();
		textbookGraphDao.deleteUser(userId);
		long endTime = System.currentTimeMillis();
		logger.log(Level.INFO, "delete existing user = " + (endTime - startTime) + "ms");

		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversalAfterDelete = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		boolean userExists = getUserTraversal.hasNext();
		assert !userExists;
	}

	@Test
	public void deleteNonExistingUserIdempotent() {
		long startTime = System.currentTimeMillis();
		textbookGraphDao.deleteUser("DoesntExist");
		long endTime = System.currentTimeMillis();
		logger.log(Level.INFO, "delete nonexising user = " + (endTime - startTime) + "ms");
	}

	@Test
	public void createTextbookTest() {
		String textbookId = UUID.randomUUID().toString();
		try {
			Textbook textbook = createTextbookObject(textbookId);
			long startTime = System.currentTimeMillis();
			boolean createdTextbook = textbookGraphDao.createTextbook(textbook);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "create new textbook = " + (endTime - startTime) + "ms");
			assert createdTextbook;

			GraphTraversal<Vertex, Map<Object, Object>> getTextbook = graphTraversalSource.V()
					.hasLabel(TEXTBOOK_LABEL)
					.has("uuid", textbookId)
					.limit(1)
					.valueMap(true);
			boolean textbookExists = getTextbook.hasNext();
			assert textbookExists;
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getTextbookReturnsTextbook() {
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			long startTime = System.currentTimeMillis();
			Textbook textbookRetrieved = textbookGraphDao.getTextbook(textbookId);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "get textbook = " + (endTime - startTime) + "ms");
			assert null != textbookRetrieved;
			assert textbookRetrieved.getUuid().equals(textbookId);
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getTextbookNonExistingTextbookReturnsNull() {
		long startTime = System.currentTimeMillis();
		Textbook doesntExist = textbookGraphDao.getTextbook("doesntExist");
		long endTime = System.currentTimeMillis();
		logger.log(Level.INFO, "get nonexisting textbook = " + (endTime - startTime) + "ms");
		assert null == doesntExist;
	}

	@Test
	public void deleteTextbookTest() {
		String textbookId = UUID.randomUUID().toString();
		Textbook textbook = createTextbookObject(textbookId);
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.property(NODE_UUID, textbook.getUuid());
		traversal.hasNext();

		GraphTraversal<Vertex, Map<Object, Object>> getTextbookTraversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.limit(1)
				.valueMap(true);
		Map<Object, Object> textbookMap = getTextbookTraversal.next();
		Textbook getTextbook = createTextbookFromMap(textbookMap);

		assert null != getTextbook;

		long startTime = System.currentTimeMillis();
		textbookGraphDao.deleteTextbook(textbookId);
		long endTime = System.currentTimeMillis();
		logger.log(Level.INFO, "delete existing textbook = " + (endTime - startTime) + "ms");

		GraphTraversal<Vertex, Map<Object, Object>> getTextbookTraversalAfterDelete = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.limit(1)
				.valueMap(true);
		boolean textbookExists = getTextbookTraversalAfterDelete.hasNext();
		assert !textbookExists;
	}

	@Test
	public void doesTextbookExistReturnsTrue() {
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			long startTime = System.currentTimeMillis();
			boolean textbookExist = textbookGraphDao.doesTextbookExist(textbookId, isbn10, isbn13);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "does textbook exist by textbookid, isbn10, isbn13 = " + (endTime - startTime) + "ms");
			assert textbookExist;
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void doesTextbookExistByIsbn10ReturnsTrue() {
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			long startTime = System.currentTimeMillis();
			boolean textbookExist = textbookGraphDao.doesTextbookExistByIsbn10(isbn10);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "does textbook exist by isbn10 = " + (endTime - startTime) + "ms");
			assert textbookExist;
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void doesTextbookExistByIsbn13ReturnsTrue() {
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			long startTime = System.currentTimeMillis();
			boolean textbookExist = textbookGraphDao.doesTextbookExistByIsbn13(isbn13);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "does textbook exist by isbn13 = " + (endTime - startTime) + "ms");
			assert textbookExist;
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Disabled
	@Test
	public void doesTextbookExistByIdReturnsTrue() {
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			long startTime = System.currentTimeMillis();
			boolean textbookExist = textbookGraphDao.doesTextbookExistById(textbookId);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "does textbook exist by id = " + (endTime - startTime) + "ms");
			assert textbookExist;
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void createUserTextbookRelationship() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			User user = createUser(userId);
			createUserTraversalAndAssert(user, graphTraversalSource);
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);

			long startTime = System.currentTimeMillis();
			boolean userTextbookRelationship = textbookGraphDao.createTextbookRelationship(userId, OWNS_VERB, textbookId);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "create textbook relationship = " + (endTime - startTime) + "ms");
			assert userTextbookRelationship;

			GraphTraversal<Vertex, Path> relationshipTraversal = graphTraversalSource.V()
					.hasLabel(USER_LABEL)
					.has(NODE_UUID, userId)
					.outE(OWNS_VERB)
					.as(RELATIONSHIP_ALIAS)
					.inV()
					.hasLabel(TEXTBOOK_LABEL)
					.has(NODE_UUID, textbookId)
					.select(RELATIONSHIP_ALIAS)
					.path()
					.by(__.valueMap(true));
			Path relationship = relationshipTraversal.next();
			assert null != relationship;
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getUserTextbookRelationship() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String verb = OWNS_VERB;
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			User user = createUser(userId);
			createUserTraversalAndAssert(user, graphTraversalSource);
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			createRelationshipAndAssert(userId, textbookId, verb, graphTraversalSource);

			long startTime = System.currentTimeMillis();
			Relationship userTextbookRelationship = textbookGraphDao.getTextbookRelationship(userId, OWNS_VERB, textbookId);
			assert userTextbookRelationship.getUser().equals(userId);
			assert userTextbookRelationship.getTextbook().equals(textbookId);
			assert userTextbookRelationship.getVerb().equals(OWNS_VERB);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "get existing textbook relationship = " + (endTime - startTime) + "ms");
			assert null != userTextbookRelationship;

			GraphTraversal<Vertex, Object> relationshipTraversal = graphTraversalSource.V()
					.hasLabel(USER_LABEL)
					.has(NODE_UUID, userId)
					.outE(OWNS_VERB)
					.as(RELATIONSHIP_ALIAS)
					.inV()
					.hasLabel(TEXTBOOK_LABEL)
					.has(NODE_UUID, textbookId)
					.select(RELATIONSHIP_ALIAS);
			Object relationship = relationshipTraversal.next();
			assert null != relationship;
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void deleteTextbookRelationship() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String verb = OWNS_VERB;
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			User user = createUser(userId);
			createUserTraversalAndAssert(user, graphTraversalSource);
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			createRelationshipAndAssert(userId, textbookId, verb, graphTraversalSource);

			GraphTraversal<Vertex, Object> relationshipTraversal = graphTraversalSource.V()
					.hasLabel(USER_LABEL)
					.has(NODE_UUID, userId)
					.outE(OWNS_VERB)
					.as(RELATIONSHIP_ALIAS)
					.inV()
					.hasLabel(TEXTBOOK_LABEL)
					.has(NODE_UUID, textbookId)
					.select(RELATIONSHIP_ALIAS);
			Object relationship = relationshipTraversal.next();
			assert null != relationship;

			long startTime = System.currentTimeMillis();
			boolean deletedRelationship = textbookGraphDao.deleteTextbookRelationship(userId, verb, textbookId);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "delete existing textbook relationship = " + (endTime - startTime) + "ms");
			assert deletedRelationship;

			GraphTraversal<Vertex, Object> relationshipTraversalAfterDeleted = graphTraversalSource.V()
					.hasLabel(USER_LABEL)
					.has(NODE_UUID, userId)
					.outE(OWNS_VERB)
					.as(RELATIONSHIP_ALIAS)
					.inV()
					.hasLabel(TEXTBOOK_LABEL)
					.has(NODE_UUID, textbookId)
					.select(RELATIONSHIP_ALIAS);
			boolean nonExistant = relationshipTraversalAfterDeleted.hasNext();
			assert !nonExistant;
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getUsersWhoOwnTextbook() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String userId4 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String verb = OWNS_VERB;
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			User user = createUser(userId);
			User user2 = createUser(userId2);
			User user3 = createUser(userId3);
			User user4 = createUser(userId4);

			createUserTraversalAndAssert(user, graphTraversalSource);
			createUserTraversalAndAssert(user2, graphTraversalSource);
			createUserTraversalAndAssert(user3, graphTraversalSource);
			createUserTraversalAndAssert(user4, graphTraversalSource);

			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);

			createRelationshipAndAssert(userId, textbookId, verb, graphTraversalSource);
			createRelationshipAndAssert(userId2, textbookId, verb, graphTraversalSource);
			createRelationshipAndAssert(userId3, textbookId, verb, graphTraversalSource);
			createRelationshipAndAssert(userId4, textbookId, verb, graphTraversalSource);

			long startTime = System.currentTimeMillis();
			List<User> userList = textbookGraphDao.getUsersWhoOwnTextbook(textbookId);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "get users who own one textbook = " + (endTime - startTime) + "ms");
			assert null != userList;
			assert userList.size() == 4;
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId2).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId3).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId4).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getUsersWhoOwnTextbooks() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String userId4 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String textbookId2 = UUID.randomUUID().toString();
		List<String> textbookIds = new ArrayList<>();
		textbookIds.add(textbookId);
		textbookIds.add(textbookId2);
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			User user = createUser(userId);
			User user2 = createUser(userId2);
			User user3 = createUser(userId3);
			User user4 = createUser(userId4);

			createUserTraversalAndAssert(user, graphTraversalSource);
			createUserTraversalAndAssert(user2, graphTraversalSource);
			createUserTraversalAndAssert(user3, graphTraversalSource);
			createUserTraversalAndAssert(user4, graphTraversalSource);

			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			createTextbookTraversalAndAssert(textbookId2, isbn10, isbn13, graphTraversalSource);

			createRelationshipAndAssert(userId, textbookId, OWNS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId2, textbookId, OWNS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId3, textbookId, OWNS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId4, textbookId, OWNS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId, textbookId2, OWNS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId2, textbookId2, OWNS_VERB, graphTraversalSource);

			long startTime = System.currentTimeMillis();
			Map<Long, List<User>> orderedUserMap = textbookGraphDao.getUsersWhoOwnTextbooks(textbookIds);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "get users who own a list of textbooks = " + (endTime - startTime) + "ms");
			assert null != orderedUserMap;
			assert orderedUserMap.size() == 2;
			assert orderedUserMap.get(2L).size() == 2;
			assert orderedUserMap.get(1L).size() == 2;
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId2).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId3).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId4).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId2).drop().iterate();
		}
	}

	@Test
	public void getUsersWhoOwnWantedTextbooks() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String userId4 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String textbookId2 = UUID.randomUUID().toString();
		String textbookId3 = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			User user = createUser(userId);
			User user2 = createUser(userId2);
			User user3 = createUser(userId3);
			User user4 = createUser(userId4);

			createUserTraversalAndAssert(user, graphTraversalSource);
			createUserTraversalAndAssert(user2, graphTraversalSource);
			createUserTraversalAndAssert(user3, graphTraversalSource);
			createUserTraversalAndAssert(user4, graphTraversalSource);

			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13, graphTraversalSource);
			createTextbookTraversalAndAssert(textbookId2, isbn10, isbn13, graphTraversalSource);
			createTextbookTraversalAndAssert(textbookId3, isbn10, isbn13, graphTraversalSource);

			createRelationshipAndAssert(userId, textbookId, WANTS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId, textbookId2, WANTS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId2, textbookId, OWNS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId2, textbookId2, OWNS_VERB, graphTraversalSource);
			createRelationshipAndAssert(userId3, textbookId, OWNS_VERB, graphTraversalSource);


			long startTime = System.currentTimeMillis();
			Map<Long, List<User>> orderedUserMap = textbookGraphDao.getUsersWhoOwnWantedTextbooks(userId);
			long endTime = System.currentTimeMillis();
			logger.log(Level.INFO, "get users who own textbooks that are wanted by user = " + (endTime - startTime) + "ms");
			assert null != orderedUserMap;
			assert orderedUserMap.size() == 2;
			assert orderedUserMap.get(2L).get(0).getUuid().equals(userId2);
			assert orderedUserMap.get(1L).get(0).getUuid().equals(userId3);
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId2).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId3).drop().iterate();
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId4).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId2).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId3).drop().iterate();
		}
	}
}