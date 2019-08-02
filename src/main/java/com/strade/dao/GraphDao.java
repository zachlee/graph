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
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

import java.util.Map;
import java.util.UUID;

public class GraphDao {

	private static ObjectMapper mapper = new ObjectMapper();
	private static GraphTraversalSource graphTraversalSource;
	private static final String TEXTBOOK_LABEL = "textbook";

	public GraphDao() {
		Cluster readCluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = EmptyGraph.instance().traversal().withRemote(DriverRemoteConnection.using(readCluster));
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
		Map<Object, Object> textbook = traversal.next();
		//todo figure out how to return the textbook
		return new Textbook();
	}

	public static boolean insertTextbook(Textbook textbook) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV();
		UUID textbookId = UUID.randomUUID();
		traversal.hasLabel(TEXTBOOK_LABEL)
				.hasId(textbookId)
				.properties("title", textbook.getTitle())
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
				.hasLabel("user")
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

	public static void testGraph() {
		Vertex v = graphTraversalSource.V().next();
	}
}
