package com.strade.integration;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

public class TestNeo4j {

	private static GraphTraversalSource graphTraversalSource;
	private static final String TEXTBOOK_LABEL = "textbook";
	private static final String USER_LABEL = "user";

	public TestNeo4j() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	@Test
	public void insertTextbookTest() {
		String textbookId = UUID.randomUUID().toString();
		Map<Object, Object> next = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.hasId(textbookId)
				.properties("title", "TITLE")
				.properties("author", "AUTHOR")
				.properties("generalSubject", "GENERAL_SUBJECT")
				.properties("specificSubject", "SPECIFIC_SUBJECT")
				.properties("isbn10", "ISBN10")
				.properties("isbn13", "ISBN13")
				.valueMap(true)
				.next();

		GraphTraversal<Vertex, Vertex> traversalRead = graphTraversalSource.V(textbookId)
				.hasLabel(TEXTBOOK_LABEL)
				.limit(1);
		Vertex textbookRead = traversalRead.next();
	}

	@Test
	public void graphTest() {
//		GraphTraversal<Vertex, Vertex> newVertex = graphTraversalSource.addV(TEXTBOOK_LABEL)
//				.property("id", "textbookId")
//				.property("title", "TITLE")
//				.property("author", "AUTHOR")
//				.property("generalSubject", "GENERAL_SUBJECT")
//				.property("specificSubject", "SPECIFIC_SUBJECT")
//				.property("isbn10", "ISBN10")
//				.property("isbn13", "ISBN13");
//		newVertex.next();

		GraphTraversal<Vertex, Map<Object, Object>> traversalRead = graphTraversalSource.V(14)
				.hasLabel(TEXTBOOK_LABEL)
				.has("id", "textbookId")
				.limit(1l)
				.valueMap(true);
		Map<Object, Object> next = traversalRead.next();
		System.out.println("inserted");
	}
}
