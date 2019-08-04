package com.strade.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strade.domain.Textbook;
import com.strade.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class GraphDao {

	private static ObjectMapper mapper = new ObjectMapper();
	private static GraphTraversalSource graphTraversalSource;
	private static final String TEXTBOOK_LABEL = "textbook";
	private static final String USER_LABEL = "user";

	public GraphDao() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = EmptyGraph.instance().traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	public static boolean doesTextbookExist( UUID textbookId ) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V(textbookId)
				.hasLabel(TEXTBOOK_LABEL)
				.limit(1);
		return traversal.hasNext();
	}

	public static Textbook getTextbook( UUID textbookId ) {
		GraphTraversal<Vertex, Map<Object, Object>> traversal = graphTraversalSource.V(textbookId)
				.hasLabel(TEXTBOOK_LABEL)
				.limit(1)
				.valueMap(true);
		Map<Object, Object> textbookValueMap = traversal.next();
		return createTextbook(textbookValueMap);
	}

	public static boolean insertTextbook(Textbook textbook) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV();
		UUID textbookId = UUID.randomUUID();
		traversal.hasLabel(TEXTBOOK_LABEL)
				.hasId(textbookId)
				.properties("title", textbook.getTitle())
				.properties("author", textbook.getAuthor())
				.properties("generalSubject", textbook.getGeneralSubject())
				.properties("specificSubject", textbook.getSpecificSubject())
				.properties("isbn10", textbook.getIsbn10())
				.properties("isbn13", textbook.getIsbn13());
		return traversal.hasNext();
	}

	public static boolean deleteTextbook( UUID textbookId ) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V(textbookId)
				.hasLabel(TEXTBOOK_LABEL)
				.drop();
		return traversal.hasNext();
	}

	public static boolean addUser ( User user ) {
		UUID userId = UUID.randomUUID();
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV();
		traversal.hasId(userId)
				.hasLabel(USER_LABEL)
				.properties("school", user.getSchool())
				.properties("email", user.getEmail())
				.properties("username", user.getUsername())
				.properties("type", user.getType());
		return traversal.hasNext();
	}

	public static boolean removeUser ( String userIdString ) {
		UUID userId = UUID.fromString(userIdString);
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.V(userId)
				.hasLabel("user")
				.drop();
		return traversal.hasNext();
	}

	public static boolean createTextbookRelationship( String user, String verb, String textbook) {
		UUID userId = UUID.fromString(user);
		UUID texbookId = UUID.fromString(textbook);
		GraphTraversal<Edge, Edge> traversal = graphTraversalSource.addE(verb)
				.from(__.V(userId))
				.to(__.V(texbookId));
		return traversal.hasNext();
	}

	private static Textbook createTextbook( Map<Object, Object> textbookValueMap ) {
		String id = (String) textbookValueMap.get(T.id);
		UUID textbookUUID = UUID.fromString(id);
		return new Textbook(textbookUUID,
				getString(textbookValueMap.get("title")),
				getString(textbookValueMap.get("author")),
				getString(textbookValueMap.get("generalSubject")),
				getString(textbookValueMap.get("specificSubject")),
				getString(textbookValueMap.get("isbn10")),
				getString(textbookValueMap.get("isbn13")));
	}

	public static String getString( Object entry ) {
		ArrayList<String> list = (ArrayList<String>) entry;
		if ( null != list && !list.isEmpty() ) {
			return list.get(0);
		} else {
			return "";
		}
	}

	public static void testGraph() {
		Vertex v = graphTraversalSource.V().next();
	}
}
