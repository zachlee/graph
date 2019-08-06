package com.strade.dao;

import com.strade.domain.Textbook;
import com.strade.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static com.strade.utils.Labels.*;

public class GraphDao {

	private static GraphTraversalSource graphTraversalSource;
	private static GraphDao instance;

	public GraphDao() {
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
		GraphTraversal<Vertex, Map<Object, Object>> traversal = graphTraversalSource.V(textbookId)
				.hasLabel(TEXTBOOK_LABEL)
				.limit(1)
				.valueMap(true);
		Map<Object, Object> textbookValueMap = traversal.next();
		return createTextbook(textbookValueMap);
	}

	public boolean insertTextbook(Textbook textbook) {
		String textbookId = UUID.randomUUID().toString();
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV()
				.hasLabel(TEXTBOOK_LABEL)
				.property(NODE_UUID, textbookId)
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

	public boolean addUser(User user) {
		String userId = UUID.randomUUID().toString();
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV()
				.hasLabel(USER_LABEL)
				.property(NODE_UUID, userId)
				.property(SCHOOL, user.getSchool())
				.property(EMAIL, user.getEmail())
				.property(USERNAME, user.getUsername())
				.property(TYPE, user.getType());
		return traversal.hasNext();
	}

	public boolean removeUser(String userId) {
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

	public boolean createTextbookRelationship(String userId, String verb, String textbookId) {
		GraphTraversal<Edge, Edge> traversal = graphTraversalSource.addE(verb)
				.from(__.V().hasLabel(USER_LABEL)
						.has(NODE_UUID, userId))
				.to(__.V().hasLabel(TEXTBOOK_LABEL)
						.has(NODE_UUID, textbookId));
		return traversal.hasNext();
	}

	public boolean removeTextbookRelationship(String userId, String verb, String texbookId) {
		GraphTraversal<Vertex, Object> dropEdgeTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.outE(verb)
				.as(USER_ALIAS)
				.inV()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, texbookId)
				.valueMap(true)
				.store(DROPPED_ALIAS)
				.select(USER_ALIAS)
				.drop()
				.cap(DROPPED_ALIAS)
				.unfold();

		return dropEdgeTraversal.hasNext();
	}

	private Textbook createTextbook(Map<Object, Object> textbookValueMap) {
		String textbookId = (String) textbookValueMap.get(T.id);
		return new Textbook(textbookId,
				getString(textbookValueMap.get(TITLE)),
				getString(textbookValueMap.get(AUTHOR)),
				getString(textbookValueMap.get(GENERAL_SUBJECT)),
				getString(textbookValueMap.get(SPECIFIC_SUBJECT)),
				getString(textbookValueMap.get(ISBN10)),
				getString(textbookValueMap.get(ISBN13)));
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
