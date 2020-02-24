package com.strade.util;

import com.strade.domain.Textbook;
import com.strade.domain.User;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Map;

import static com.strade.utils.Labels.*;

public class TestUtils {

	public static void createRelationshipAndAssert(String userId, String textbookId, String verb, GraphTraversalSource graphTraversalSource) {
		GraphTraversal<Edge, Edge> createEdgeTraversal = graphTraversalSource.addE(verb)
				.from(__.V().hasLabel(USER_LABEL)
						.has(NODE_UUID, userId))
				.to(__.V().hasLabel(TEXTBOOK_LABEL)
						.has(NODE_UUID, textbookId));
		Edge edge = createEdgeTraversal.next();
		assert null != edge;
	}

	public static void createTextbookTraversalAndAssert(String textbookId, String isbn10, String isbn13, GraphTraversalSource graphTraversalSource) {
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

	public static void createUserTraversalAndAssert(User user, GraphTraversalSource graphTraversalSource) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(USER_LABEL)
				.property(NODE_UUID, user.getUuid())
				.property(SCHOOL, user.getSchool())
				.property(EMAIL, user.getEmail())
				.property(USERNAME, user.getUsername())
				.property(TYPE, user.getType());
		assert traversal.hasNext();
	}

	public static boolean getUserTextbookRelationshipExists(String userId, String verb, String textbookId, GraphTraversalSource graphTraversalSource) {
		GraphTraversal<Vertex, Path> relationshipTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.outE(verb)
				.as(RELATIONSHIP_ALIAS)
				.inV()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.select(RELATIONSHIP_ALIAS)
				.path()
				.by(__.valueMap(true));
		return relationshipTraversal.hasNext();
	}

	public static Textbook createTextbookObject(String textbookId) {
		return new Textbook(textbookId,
				"title",
				"author",
				"isbn10",
				"isbn13");
	}

	public static Textbook createTextbookFromMap(Map<Object, Object> textbookValueMap) {
		return new Textbook(getString(textbookValueMap.get(NODE_UUID)),
				getString(textbookValueMap.get(TITLE)),
				getString(textbookValueMap.get(AUTHOR)),
				getString(textbookValueMap.get(ISBN10)),
				getString(textbookValueMap.get(ISBN13)));
	}

	public static User createUser(String uuid) {
		return new User(uuid,
				"username",
				"email",
				"school",
				"type");
	}

	public static User createUserFromMap(Map<Object, Object> userValueMap) {
		return new User(getString(userValueMap.get(NODE_UUID)),
				getString(userValueMap.get(USERNAME)),
				getString(userValueMap.get(EMAIL)),
				getString(userValueMap.get(SCHOOL)),
				getString(userValueMap.get(TYPE)));
	}

	public static boolean doesUserExist(String userId, GraphTraversalSource graphTraversalSource) {
		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		return getUserTraversal.hasNext();
	}

	public static boolean doesTextbookExist(String textbookId, GraphTraversalSource graphTraversalSource) {
		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has("uuid", textbookId)
				.limit(1)
				.valueMap(true);
		return getUserTraversal.hasNext();
	}

	public static String getString(Object entry) {
		ArrayList<String> list = (ArrayList<String>) entry;
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		} else {
			return "";
		}
	}
}
