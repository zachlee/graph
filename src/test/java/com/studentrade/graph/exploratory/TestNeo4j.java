package com.studentrade.graph.exploratory;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.studentrade.graph.dao.TextbookGraphDao;
import com.studentrade.graph.domain.User;
import com.studentrade.graph.dao.TextbookGraphDaoImpl;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static com.studentrade.graph.util.Labels.*;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;


public class TestNeo4j {

	Logger logger = Logger.getLogger(TestNeo4j.class.getName());
	private static GraphTraversalSource graphTraversalSource;
	private static TextbookGraphDao textbookGraphDao = new TextbookGraphDaoImpl();
	private static final String TEXTBOOK_LABEL = "textbook";
	private static final String USER_LABEL = "user";

	private static final DynamicStringProperty GREMLIN_DOMAIN = DynamicPropertyFactory.getInstance()
			.getStringProperty("gremlin.server.domain", "localhost");

	public TestNeo4j() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint(GREMLIN_DOMAIN.get()).create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	@Test
	public void insertReadDropVertexTest() {
		String textbookId = UUID.randomUUID().toString();
		GraphTraversal<Vertex, Map<Object, Object>> vertexInsert = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.property(NODE_UUID, textbookId)
				.property(TITLE, "TITLE")
				.property(AUTHOR, "AUTHOR")
				.property(GENERAL_SUBJECT, "GENERAL_SUBJECT")
				.property(SPECIFIC_SUBJECT, "SPECIFIC_SUBJECT")
				.property(ISBN10, "ISBN10")
				.property(ISBN13, "ISBN13")
				.valueMap(true);
		Map<Object, Object> insertedVertex = vertexInsert.next();

		assert insertedVertex.size() == 9;

		GraphTraversal<Vertex, Map<Object, Object>> traversalRead = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.limit(1)
				.valueMap(true);
		boolean textbookRead = traversalRead.hasNext();
		assert (textbookRead);

		GraphTraversal<Vertex, Vertex> dropTraversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.as(TEXTBOOK_ALIAS)
				.valueMap(true)
				.store(DROPPED_ALIAS)
				.select(TEXTBOOK_ALIAS)
				.drop()
				.cap(DROPPED_ALIAS)
				.unfold();
		boolean dropped = dropTraversal.hasNext();
		assert dropped;
	}

	@Test
	public void insertReadDropEdge() {
		String idOne = UUID.randomUUID().toString() + "_test";
		Vertex vertexOne = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.property(NODE_UUID, idOne)
				.next();
		assert null != vertexOne;
		String idTwo = UUID.randomUUID().toString() + "_test";
		Vertex vertexTwo = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.property(NODE_UUID, idTwo)
				.next();
		assert null != vertexTwo;

		GraphTraversal<Edge, Edge> edgeTraversal = graphTraversalSource.addE(OWNS_VERB)
				.from(__.V().hasLabel(TEXTBOOK_LABEL)
						.has(NODE_UUID, idOne))
				.to(__.V().hasLabel(TEXTBOOK_LABEL)
						.has(NODE_UUID, idTwo));
		Edge next = edgeTraversal.next();
		assert null != next;

//		GraphTraversal<Vertex, Object> dropEdgeTraversal = graphTraversalSource.V()
//				.hasLabel(TEXTBOOK_LABEL)
//				.has(NODE_UUID, idOne)
//				.outE(OWNS_VERB)
//				.as(OWNS_ALIAS)
//				.inV()
//				.hasLabel(TEXTBOOK_LABEL)
//				.has(NODE_UUID, idTwo)
//				.valueMap(true)
//				.store(DROPPED_ALIAS)
//				.select(OWNS_ALIAS)
//				.drop()
//				.cap(DROPPED_ALIAS)
//				.unfold();
//
//		boolean droppedEdge = dropEdgeTraversal.hasNext();
//		assert droppedEdge;
//
//		GraphTraversal<Vertex, Vertex> dropTraversal = graphTraversalSource.V()
//				.hasLabel(TEXTBOOK_LABEL)
//				.has(NODE_UUID, idOne)
//				.as(TEXTBOOK_ALIAS)
//				.valueMap(true)
//				.store(DROPPED_ALIAS)
//				.select(TEXTBOOK_ALIAS)
//				.drop()
//				.cap(DROPPED_ALIAS)
//				.unfold();
//		boolean droppedOne = dropTraversal.hasNext();
//		assert droppedOne;
//
//		GraphTraversal<Vertex, Vertex> dropTraversalTwo = graphTraversalSource.V()
//				.hasLabel(TEXTBOOK_LABEL)
//				.has(NODE_UUID, idTwo)
//				.as(TEXTBOOK_ALIAS)
//				.valueMap(true)
//				.store(DROPPED_ALIAS)
//				.select(TEXTBOOK_ALIAS)
//				.drop()
//				.cap(DROPPED_ALIAS)
//				.unfold();
//		boolean droppedTwo = dropTraversalTwo.hasNext();
//		assert droppedTwo;
	}

	@Test
	public void pathTest() {
		String userId = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		try {
			User user = createUser(userId);
			GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(USER_LABEL)
					.property(NODE_UUID, user.getUuid())
					.property(SCHOOL, user.getSchool())
					.property(EMAIL, user.getEmail())
					.property(USERNAME, user.getUsername())
					.property(TYPE, user.getType());
			assert traversal.hasNext();

			String isbn10 = "isbn10";
			String isbn13 = "isbn13";
			GraphTraversal<Vertex, Vertex> textbookTraversal = graphTraversalSource.addV(TEXTBOOK_LABEL)
					.property(NODE_UUID, textbookId)
					.property(TITLE, "TITLE")
					.property(AUTHOR, "AUTHOR")
					.property(GENERAL_SUBJECT, "GENERAL_SUBJECT")
					.property(SPECIFIC_SUBJECT, "SPECIFIC_SUBJECT")
					.property(ISBN10, isbn10)
					.property(ISBN13, isbn13);
			assert textbookTraversal.hasNext();

			boolean userTextbookRelationship = textbookGraphDao.createTextbookRelationship(userId, OWNS_VERB, textbookId);
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
					.by(valueMap(true));
			Path relationship = relationshipTraversal.next();
			List<Object> objects = relationship.objects();
			Map<String, List<String>> userMap = (HashMap<String, List<String>>) objects.get(0);
			List<String> userUUIDList = userMap.get("uuid");
			String userIdFromList = userUUIDList.get(0);
			Map<String, List<String>> textbookMap = (HashMap<String, List<String>>) objects.get(2);
			List<String> textbookList = textbookMap.get("uuid");
			String textbookIdFromList = textbookList.get(0);
			HashMap<T, String> edgeMap = (HashMap<T, String>) objects.get(1);
			String edgeLabel = edgeMap.entrySet().iterator().next().getValue();
			assert null != relationship;
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	@Test
	public void populateVertex() {
		String userId = UUID.randomUUID().toString();
		String userId2 = UUID.randomUUID().toString();
		String userId3 = UUID.randomUUID().toString();
		String userId4 = UUID.randomUUID().toString();
		String textbookId = UUID.randomUUID().toString();
		String textbookId2 = UUID.randomUUID().toString();
		try {
			for (int i = 0; i < 1000; i++) {

				User user = createUser(userId);
				GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(USER_LABEL);
				traversal.hasNext();

				User user2 = createUser(userId2);
				GraphTraversal<Vertex, Vertex> traversal2 = graphTraversalSource.addV(USER_LABEL)
						.property(NODE_UUID, UUID.randomUUID().toString())
						.property(SCHOOL, user2.getSchool())
						.property(EMAIL, user2.getEmail())
						.property(USERNAME, "kelly")
						.property(TYPE, user2.getType());
				traversal2.hasNext();

				User user3 = createUser(userId3);
				GraphTraversal<Vertex, Vertex> traversal3 = graphTraversalSource.addV(USER_LABEL)
						.property(NODE_UUID, UUID.randomUUID().toString())
						.property(SCHOOL, user3.getSchool())
						.property(EMAIL, user3.getEmail())
						.property(USERNAME, "josh")
						.property(TYPE, user3.getType());
				traversal3.hasNext();

				User user4 = createUser(userId4);
				GraphTraversal<Vertex, Vertex> traversal4 = graphTraversalSource.addV(USER_LABEL)
						.property(NODE_UUID, UUID.randomUUID().toString())
						.property(SCHOOL, user4.getSchool())
						.property(EMAIL, user4.getEmail())
						.property(USERNAME, "haley")
						.property(TYPE, user4.getType());
				traversal4.hasNext();

				String isbn10 = "isbn10";
				String isbn13 = "isbn13";
				GraphTraversal<Vertex, Vertex> textbookTraversal = graphTraversalSource.addV(TEXTBOOK_LABEL)
						.property(NODE_UUID, UUID.randomUUID().toString())
						.property(TITLE, "Calculus")
						.property(AUTHOR, "Issac")
						.property(GENERAL_SUBJECT, "Math")
						.property(SPECIFIC_SUBJECT, "Calculus")
						.property(ISBN10, isbn10)
						.property(ISBN13, isbn13);
				textbookTraversal.hasNext();

				GraphTraversal<Vertex, Vertex> textbookTraversal2 = graphTraversalSource.addV(TEXTBOOK_LABEL)
						.property(NODE_UUID, UUID.randomUUID().toString())
						.property(TITLE, "English")
						.property(AUTHOR, "Shakespear")
						.property(GENERAL_SUBJECT, "Language")
						.property(SPECIFIC_SUBJECT, "Literature")
						.property(ISBN10, isbn10)
						.property(ISBN13, isbn13);
				textbookTraversal2.hasNext();

				GraphTraversal<Vertex, Vertex> textbookTraversal3 = graphTraversalSource.addV(TEXTBOOK_LABEL)
						.property(NODE_UUID, UUID.randomUUID().toString())
						.property(TITLE, "Physics")
						.property(AUTHOR, "Tyson")
						.property(GENERAL_SUBJECT, "Science")
						.property(SPECIFIC_SUBJECT, "Physics")
						.property(ISBN10, isbn10)
						.property(ISBN13, isbn13);
				textbookTraversal3.hasNext();

				GraphTraversal<Edge, Edge> createEdgeTraversal = graphTraversalSource.addE(OWNS_VERB)
						.from(__.V().hasLabel(USER_LABEL)
								.has(NODE_UUID, userId))
						.to(__.V().hasLabel(TEXTBOOK_LABEL)
								.has(NODE_UUID, textbookId));
				Edge edge = createEdgeTraversal.next();
//				assert null != edge;


				GraphTraversal<Edge, Edge> createEdgeTraversal2 = graphTraversalSource.addE(OWNS_VERB)
						.from(__.V().hasLabel(USER_LABEL)
								.has(NODE_UUID, userId2))
						.to(__.V().hasLabel(TEXTBOOK_LABEL)
								.has(NODE_UUID, textbookId));
				Edge edge2 = createEdgeTraversal2.next();
//				assert null != edge2;

				GraphTraversal<Edge, Edge> createEdgeTraversal3 = graphTraversalSource.addE(OWNS_VERB)
						.from(__.V().hasLabel(USER_LABEL)
								.has(NODE_UUID, userId3))
						.to(__.V().hasLabel(TEXTBOOK_LABEL)
								.has(NODE_UUID, textbookId));
				Edge edge3 = createEdgeTraversal3.next();
//				assert null != edge3;


				GraphTraversal<Edge, Edge> createEdgeTraversal4 = graphTraversalSource.addE(OWNS_VERB)
						.from(__.V().hasLabel(USER_LABEL)
								.has(NODE_UUID, userId4))
						.to(__.V().hasLabel(TEXTBOOK_LABEL)
								.has(NODE_UUID, textbookId));
				Edge edge4 = createEdgeTraversal4.next();
//				assert null != edge4;

				GraphTraversal<Edge, Edge> createEdgeTraversal5 = graphTraversalSource.addE(OWNS_VERB)
						.from(__.V().hasLabel(USER_LABEL)
								.has(NODE_UUID, userId))
						.to(__.V().hasLabel(TEXTBOOK_LABEL)
								.has(NODE_UUID, textbookId2));
				Edge edge5 = createEdgeTraversal5.next();
//				assert null != edge;


				GraphTraversal<Edge, Edge> createEdgeTraversal6 = graphTraversalSource.addE(OWNS_VERB)
						.from(__.V().hasLabel(USER_LABEL)
								.has(NODE_UUID, userId2))
						.to(__.V().hasLabel(TEXTBOOK_LABEL)
								.has(NODE_UUID, textbookId2));
				Edge edge6 = createEdgeTraversal6.next();
				assert null != edge2;
			}

//			List<User> userList = graphDao.getUsersWhoOwnTextbook(textbookId);
//			assert null != userList;
//			assert userList.size() == 4;
		} finally {
//			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
//			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId2).drop().iterate();
//			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId3).drop().iterate();
//			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId4).drop().iterate();
//			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
//			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId2).drop().iterate();
		}
	}

	@Test
	public void getTextbookRelationshipsInOrder() {
		GraphTraversal<Vertex, List<Map<Object, Long>>> vertexListGraphTraversal = graphTraversalSource.V().has("uuid", "u1").out("wants").in("owns").valueMap().groupCount().order().fold();
		List<Map<Object, Long>> next = vertexListGraphTraversal.next();
		System.out.println("next");
	}

	private User createUser(String uuid) {
		return new User(uuid,
				"username",
				"email",
				"school",
				"type");
	}
}
