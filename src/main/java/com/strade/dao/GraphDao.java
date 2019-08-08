package com.strade.dao;

import com.strade.domain.Relationship;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static com.strade.utils.Labels.*;

public class GraphDao {

	private static GraphTraversalSource graphTraversalSource;
	private static GraphDao instance;

	GraphDao() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	public static GraphDao getInstance() {
		if (instance == null)
			instance = new GraphDao();
		return instance;
	}

	public boolean doesTextbookExistById(String textbookId) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId);
		return traversal.hasNext();
	}

	public boolean doesTextbookExistByIsbn10(String isbn10) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(ISBN10, isbn10);
		return traversal.hasNext();
	}

	public boolean doesTextbookExistByIsbn13(String isbn13) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(ISBN13, isbn13);
		return traversal.hasNext();
	}

	public boolean doesTextbookExist(String textbookId, String isbn10, String isbn13) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V()
				.or(__.V().hasLabel(TEXTBOOK_LABEL).has(NODE_UUID, textbookId),
					__.V().hasLabel(TEXTBOOK_LABEL).has(ISBN10, isbn10),
					__.V().hasLabel(TEXTBOOK_LABEL).has(ISBN13, isbn13));
		return traversal.hasNext();
	}

	public boolean doesUserExist(String userId) {
		return true;
	}

	public boolean isVerbValid(String verbId) {
		return true;
	}

	public Textbook getTextbook(String textbookId) {
		GraphTraversal<Vertex, Map<Object, Object>> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.property(NODE_UUID, textbookId)
				.limit(1)
				.valueMap(true);
		if ( traversal.hasNext() ) {
			Map<Object, Object> textbookValueMap = traversal.next();
			return createTextbook(textbookValueMap);
		} else {
			return null;
		}
	}

	public boolean createTextbook(Textbook textbook) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.property(NODE_UUID, textbook.getUuid())
				.property(TITLE, textbook.getTitle())
				.property(AUTHOR, textbook.getAuthor())
				.property(GENERAL_SUBJECT, textbook.getGeneralSubject())
				.property(SPECIFIC_SUBJECT, textbook.getSpecificSubject())
				.property(ISBN10, textbook.getIsbn10())
				.property(ISBN13, textbook.getIsbn13());
		return traversal.hasNext();
	}

	public boolean deleteTextbook(String textbookId) {
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
		return dropTraversal.hasNext();
	}

	public boolean createUser(User user) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(USER_LABEL)
				.property(NODE_UUID, user.getUuid())
				.property(SCHOOL, user.getSchool())
				.property(EMAIL, user.getEmail())
				.property(USERNAME, user.getUsername())
				.property(TYPE, user.getType());
		return traversal.hasNext();
	}

	public boolean deleteUser(String userId) {
		GraphTraversal<Vertex, Vertex> dropTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.as(USER_ALIAS)
				.valueMap(true)
				.store(DROPPED_ALIAS)
				.select(USER_ALIAS)
				.drop()
				.cap(DROPPED_ALIAS)
				.unfold();
		return dropTraversal.hasNext();
	}

	public User getUser(String userId) {
		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		if (getUserTraversal.hasNext()) {
			Map<Object, Object> user = getUserTraversal.next();
			return createUser(user);
		} else {
			return null;
		}
	}

	public boolean createTextbookRelationship(String userId, String verb, String textbookId) {
		GraphTraversal<Edge, Edge> traversal = graphTraversalSource.addE(verb)
				.from(__.V().hasLabel(USER_LABEL)
						.has(NODE_UUID, userId))
				.to(__.V().hasLabel(TEXTBOOK_LABEL)
						.has(NODE_UUID, textbookId));
		return traversal.hasNext();
	}

	public boolean deleteTextbookRelationship(String userId, String verb, String texbookId) {
		GraphTraversal<Vertex, Object> dropEdgeTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.outE(verb)
				.as(RELATIONSHIP_ALIAS)
				.inV()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, texbookId)
				.valueMap(true)
				.store(DROPPED_ALIAS)
				.select(RELATIONSHIP_ALIAS)
				.drop()
				.cap(DROPPED_ALIAS)
				.unfold();

		return dropEdgeTraversal.hasNext();
	}

	public Relationship getTextbookRelationship(String userId, String verb, String textbookId) {
		GraphTraversal<Vertex, Map<Object, Object>> relationshipTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.outE(verb)
				.as(RELATIONSHIP_ALIAS)
				.inV()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.select(RELATIONSHIP_ALIAS)
				.valueMap(true);
		Map<Object, Object> relationshipMap = relationshipTraversal.next();
		return createRelationship(relationshipMap);
	}

	private Textbook createTextbook(Map<Object, Object> textbookValueMap) {
		return new Textbook(getString(textbookValueMap.get(NODE_UUID)),
				getString(textbookValueMap.get(TITLE)),
				getString(textbookValueMap.get(AUTHOR)),
				getString(textbookValueMap.get(GENERAL_SUBJECT)),
				getString(textbookValueMap.get(SPECIFIC_SUBJECT)),
				getString(textbookValueMap.get(ISBN10)),
				getString(textbookValueMap.get(ISBN13)));
	}

	private Relationship createRelationship(Map<Object, Object> relationshipValueMap) {
		return new Relationship(getString(relationshipValueMap.get(NODE_UUID)),
				getString(relationshipValueMap.get(USER_LABEL)),
				getString(relationshipValueMap.get(VERB_LABEL)),
				getString(relationshipValueMap.get(TEXTBOOK_LABEL)));
	}

	private User createUser(Map<Object, Object> userValueMap) {
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
