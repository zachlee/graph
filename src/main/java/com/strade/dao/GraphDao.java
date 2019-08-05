package com.strade.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
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

public class GraphDao {

	private static GraphTraversalSource graphTraversalSource;
	private static GraphDao instance;

	private static final String TEXTBOOK_LABEL = "textbook";
	private static final String USER_LABEL = "user";

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
				.has("id", textbookId);
		return traversal.hasNext();
	}

	public boolean doesTextbookExistByIsbn10(String isbn10) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has("isbn10", isbn10);
		return traversal.hasNext();
	}

	public boolean doesTextbookExistByIsbn13(String isbn13) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has("isbn13", isbn13);
		return traversal.hasNext();
	}

	public boolean doesTextbookExist(String textbookId, String isbn10, String isbn13) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V()
				.or(__.V().hasLabel(TEXTBOOK_LABEL).has("id", textbookId),
					__.V().hasLabel(TEXTBOOK_LABEL).has("isbn10", isbn10),
					__.V().hasLabel(TEXTBOOK_LABEL).has("isbn13", isbn13));
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
				.property("id", textbookId)
				.property("title", textbook.getTitle())
				.property("author", textbook.getAuthor())
				.property("generalSubject", textbook.getGeneralSubject())
				.property("specificSubject", textbook.getSpecificSubject())
				.property("isbn10", textbook.getIsbn10())
				.property("isbn13", textbook.getIsbn13());
		return traversal.hasNext();
	}

	public boolean deleteTextbook(String textbookId) {
		GraphTraversal<Vertex, Vertex> dropTraversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has("id", textbookId)
				.as("foundVertex")
				.valueMap(true)
				.store("droppedVertex")
				.select("foundVertex")
				.drop()
				.cap("droppedVertex")
				.unfold();
		return dropTraversal.hasNext();
	}

	public boolean addUser(User user) {
		String userId = UUID.randomUUID().toString();
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV()
				.hasLabel(USER_LABEL)
				.property("id", userId)
				.property("school", user.getSchool())
				.property("email", user.getEmail())
				.property("username", user.getUsername())
				.property("type", user.getType());
		return traversal.hasNext();
	}

	public boolean removeUser(String userId) {
		GraphTraversal<Vertex, Vertex> dropTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("id", userId)
				.as("foundVertex")
				.valueMap(true)
				.store("droppedVertex")
				.select("foundVertex")
				.drop()
				.cap("droppedVertex")
				.unfold();
		return dropTraversal.hasNext();
	}

	public boolean createTextbookRelationship(String userId, String verb, String textbookId) {
		GraphTraversal<Edge, Edge> traversal = graphTraversalSource.addE(verb)
				.from(__.V().hasLabel(USER_LABEL)
						.has("id", userId))
				.to(__.V().hasLabel(TEXTBOOK_LABEL)
						.has("id", textbookId));
		return traversal.hasNext();
	}

	public boolean removeTextbookRelationship(String userId, String verb, String texbookId) {
		GraphTraversal<Vertex, Object> dropEdgeTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("id", userId)
				.outE(verb)
				.as("foundEdge")
				.inV()
				.hasLabel(TEXTBOOK_LABEL)
				.has("id", texbookId)
				.valueMap(true)
				.store("droppedEdge")
				.select("foundEdge")
				.drop()
				.cap("droppedEdge")
				.unfold();

		return dropEdgeTraversal.hasNext();
	}

	private Textbook createTextbook(Map<Object, Object> textbookValueMap) {
		String textbookId = (String) textbookValueMap.get(T.id);
		return new Textbook(textbookId,
				getString(textbookValueMap.get("title")),
				getString(textbookValueMap.get("author")),
				getString(textbookValueMap.get("generalSubject")),
				getString(textbookValueMap.get("specificSubject")),
				getString(textbookValueMap.get("isbn10")),
				getString(textbookValueMap.get("isbn13")));
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
