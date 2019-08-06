package com.strade.integration;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.Map;
import java.util.UUID;

import static com.strade.utils.Labels.*;

public class TestNeo4j {

	private static GraphTraversalSource graphTraversalSource;
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
}
