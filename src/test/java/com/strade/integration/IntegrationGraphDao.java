package com.strade.integration;

import com.strade.dao.GraphDao;
import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.strade.utils.Labels.*;
import static com.strade.utils.Labels.SCHOOL;
import static com.strade.utils.Labels.TYPE;

public class IntegrationGraphDao {

	GraphDao graphDao = GraphDao.getInstance();
	private static GraphTraversalSource graphTraversalSource;

	@BeforeClass
	public static void setup() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	@Test
	public void createUserTest() {
		String userId = UUID.randomUUID().toString();
		try {
			User user = createUser(userId);
			boolean addUser = graphDao.createUser(user);
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
			createUserTraversalAndAssert(user);
			User daoUser = graphDao.getUser(userId);
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
		User nonExistentUser = graphDao.getUser("DoesntExist");
		assert null == nonExistentUser;
	}

	@Test
	public void deleteUserTest() {
		String userId = UUID.randomUUID().toString();
		User user = createUser(userId);
		createUserTraversalAndAssert(user);
		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		Map<Object, Object> userMap = getUserTraversal.next();
		User getUser = createUserFromMap(userMap);

		assert null != getUser;

		graphDao.deleteUser(userId);

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
		graphDao.deleteUser("DoesntExist");
	}

	@Test
	public void createTextbookTest() {
		String textbookId = UUID.randomUUID().toString();
		try {
			Textbook textbook = createTextbookObject(textbookId);
			boolean createdTextbook = graphDao.createTextbook(textbook);
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
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			Textbook textbookRetrieved = graphDao.getTextbook(textbookId);
			assert null != textbookRetrieved;
			assert textbookRetrieved.getUuid().equals(textbookId);
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void getTextbookNonExistingTextbookReturnsNull() {
		Textbook doesntExist = graphDao.getTextbook("doesntExist");
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

		graphDao.deleteTextbook(textbookId);

		GraphTraversal<Vertex, Map<Object, Object>> getTextbookTraversalAfterDelete = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.limit(1)
				.valueMap(true);
		boolean textbookExists = getTextbookTraversal.hasNext();
		assert !textbookExists;
	}

	@Test
	public void doesTextbookExistReturnsTrue() {
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			boolean textbookExist = graphDao.doesTextbookExist(textbookId, isbn10, isbn13);
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
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			boolean textbookExist = graphDao.doesTextbookExistByIsbn10(isbn10);
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
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			boolean textbookExist = graphDao.doesTextbookExistByIsbn13(isbn13);
			assert textbookExist;
		} finally {
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void doesTextbookExistByIdReturnsTrue() {
		String textbookId = UUID.randomUUID().toString();
		String isbn10 = "isbn10";
		String isbn13 = "isbn13";
		try {
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			boolean textbookExist = graphDao.doesTextbookExistById(textbookId);
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
			createUserTraversalAndAssert(user);
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);

			boolean userTextbookRelationship = graphDao.createTextbookRelationship(userId, OWNS_VERB, textbookId);
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
			createUserTraversalAndAssert(user);
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			createRelationshipAndAssert(userId, textbookId, verb);

			Relationship userTextbookRelationship = graphDao.getTextbookRelationship(userId, OWNS_VERB, textbookId);
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
			createUserTraversalAndAssert(user);
			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			createRelationshipAndAssert(userId, textbookId, verb);

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

			boolean deletedRelationship = graphDao.deleteTextbookRelationship(userId, verb, textbookId);
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

			createUserTraversalAndAssert(user);
			createUserTraversalAndAssert(user2);
			createUserTraversalAndAssert(user3);
			createUserTraversalAndAssert(user4);

			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);

			createRelationshipAndAssert(userId, textbookId);
			createRelationshipAndAssert(userId2, textbookId, verb);
			createRelationshipAndAssert(userId3, textbookId, verb);
			createRelationshipAndAssert(userId4, textbookId, verb);

			List<User> userList = graphDao.getUsersWhoOwnTextbook(textbookId);
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

			createUserTraversalAndAssert(user);
			createUserTraversalAndAssert(user2);
			createUserTraversalAndAssert(user3);
			createUserTraversalAndAssert(user4);

			createTextbookTraversalAndAssert(textbookId, isbn10, isbn13);
			createTextbookTraversalAndAssert(textbookId2, isbn10, isbn13);

			createRelationshipAndAssert(userId, textbookId, OWNS_VERB);
			createRelationshipAndAssert(userId2, textbookId, OWNS_VERB);
			createRelationshipAndAssert(userId3, textbookId, OWNS_VERB);
			createRelationshipAndAssert(userId4, textbookId, OWNS_VERB);
			createRelationshipAndAssert(userId, textbookId2, OWNS_VERB);
			createRelationshipAndAssert(userId2, textbookId2, OWNS_VERB);

			Map<Long, List<User>> orderedUserMap = graphDao.getUsersWhoOwnTextbooks(textbookIds);
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

	private void createRelationshipAndAssert(String userId2, String textbookId, String verb) {
		GraphTraversal<Edge, Edge> createEdgeTraversal2 = graphTraversalSource.addE(verb)
				.from(__.V().hasLabel(USER_LABEL)
						.has(NODE_UUID, userId2))
				.to(__.V().hasLabel(TEXTBOOK_LABEL)
						.has(NODE_UUID, textbookId));
		Edge edge2 = createEdgeTraversal2.next();
		assert null != edge2;
	}

	private void createRelationshipAndAssert(String userId, String textbookId) {
		createRelationshipAndAssert(userId, textbookId, OWNS_VERB);
	}

	private void createTextbookTraversalAndAssert(String textbookId, String isbn10, String isbn13) {
		GraphTraversal<Vertex, Vertex> textbookTraversal = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.property(NODE_UUID, textbookId)
				.property(TITLE, "TITLE")
				.property(AUTHOR, "AUTHOR")
				.property(GENERAL_SUBJECT, "GENERAL_SUBJECT")
				.property(SPECIFIC_SUBJECT, "SPECIFIC_SUBJECT")
				.property(ISBN10, isbn10)
				.property(ISBN13, isbn13);
		assert textbookTraversal.hasNext();
	}

	private void createUserTraversalAndAssert(User user) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(USER_LABEL)
				.property(NODE_UUID, user.getUuid())
				.property(SCHOOL, user.getSchool())
				.property(EMAIL, user.getEmail())
				.property(USERNAME, user.getUsername())
				.property(TYPE, user.getType());
		assert traversal.hasNext();
	}

	private Textbook createTextbookObject(String textbookId) {
		return new Textbook(textbookId,
				"title",
				"author",
				"subject",
				"specificSubject",
				"isbn10",
				"isbn13");
	}

	private Textbook createTextbookFromMap(Map<Object, Object> textbookValueMap) {
		return new Textbook(getString(textbookValueMap.get(NODE_UUID)),
				getString(textbookValueMap.get(TITLE)),
				getString(textbookValueMap.get(AUTHOR)),
				getString(textbookValueMap.get(GENERAL_SUBJECT)),
				getString(textbookValueMap.get(SPECIFIC_SUBJECT)),
				getString(textbookValueMap.get(ISBN10)),
				getString(textbookValueMap.get(ISBN13)));
	}

	private User createUser(String uuid) {
		return new User(uuid,
				"username",
				"email",
				"school",
				"type");
	}

	private User createUserFromMap(Map<Object, Object> userValueMap) {
		return new User(getString(userValueMap.get(NODE_UUID)),
				getString(userValueMap.get(USERNAME)),
				getString(userValueMap.get(EMAIL)),
				getString(userValueMap.get(SCHOOL)),
				getString(userValueMap.get(TYPE)));
	}

	private String getString(Object entry) {
		ArrayList<String> list = (ArrayList<String>) entry;
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		} else {
			return "";
		}
	}
}
