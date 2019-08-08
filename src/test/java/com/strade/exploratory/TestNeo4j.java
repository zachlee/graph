package com.strade.exploratory;

import com.strade.dao.GraphDao;
import com.strade.domain.User;
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
import org.junit.Test;

import java.util.*;

import static com.strade.utils.Labels.*;
import static com.strade.utils.Labels.TEXTBOOK_LABEL;
import static com.strade.utils.Labels.USER_LABEL;

public class TestNeo4j {

	private static GraphTraversalSource graphTraversalSource;
	private static GraphDao graphDao = GraphDao.getInstance();
	private static final String TEXTBOOK_LABEL = "textbook";
	private static final String USER_LABEL = "user";

	public TestNeo4j() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	@Test
	public void insertReadDropVertexTest() {
		String textbookId = UUID.randomUUID().toString() + "_test";
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
		assert(textbookRead);

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

		GraphTraversal<Vertex, Object> dropEdgeTraversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, idOne)
				.outE(OWNS_VERB)
				.as(OWNS_ALIAS)
				.inV()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, idTwo)
				.valueMap(true)
				.store(DROPPED_ALIAS)
				.select(OWNS_ALIAS)
				.drop()
				.cap(DROPPED_ALIAS)
				.unfold();

		boolean droppedEdge = dropEdgeTraversal.hasNext();
		assert droppedEdge;

		GraphTraversal<Vertex, Vertex> dropTraversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, idOne)
				.as(TEXTBOOK_ALIAS)
				.valueMap(true)
				.store(DROPPED_ALIAS)
				.select(TEXTBOOK_ALIAS)
				.drop()
				.cap(DROPPED_ALIAS)
				.unfold();
		boolean droppedOne = dropTraversal.hasNext();
		assert droppedOne;

		GraphTraversal<Vertex, Vertex> dropTraversalTwo = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, idTwo)
				.as(TEXTBOOK_ALIAS)
				.valueMap(true)
				.store(DROPPED_ALIAS)
				.select(TEXTBOOK_ALIAS)
				.drop()
				.cap(DROPPED_ALIAS)
				.unfold();
		boolean droppedTwo = dropTraversalTwo.hasNext();
		assert droppedTwo;
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
			List<Object> objects = relationship.objects();
			Map<String, List<String>> userMap = (HashMap<String, List<String>>) objects.get(0);
			List<String> userUUIDList = userMap.get("uuid");
			String userIdFromList = userUUIDList.get(0);
			Map<String, List<String>> textbookMap = (HashMap<String, List<String>>) objects.get(2);
			List<String> textbookList = textbookMap.get("uuid");
			String textbookIdFromList = textbookList.get(0);
			HashMap<T,String> edgeMap = (HashMap<T,String>) objects.get(1);
			String edgeLabel = edgeMap.entrySet().iterator().next().getValue();
			assert null != relationship;
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
			graphTraversalSource.V().hasLabel(TEXTBOOK_LABEL).has("uuid", textbookId).drop().iterate();
		}
	}

	private User createUser(String uuid) {
		return new User(uuid,
				"username",
				"email",
				"school",
				"type");
	}
}
