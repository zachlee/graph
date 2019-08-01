package com.strade.dao;

import com.strade.domain.Textbook;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

public class GraphDao {

	private static GraphTraversalSource graphTraversalSource;

	public GraphDao() {
		Cluster readCluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = EmptyGraph.instance().traversal().withRemote(DriverRemoteConnection.using(readCluster));
	}

	public static void insertTextbook(Textbook textbook) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV();
		traversal.properties("title", textbook.getTitle())
				.properties("generalSubject", textbook.getGeneralSubject())
				.properties("specificSubject", textbook.getSpecificSubject())
				.properties("isbn10", textbook.getIsbn10())
				.properties("isbn13", textbook.getIsbn13());
		traversal.next();
	}

	public static void testGraph() {
		Vertex v = graphTraversalSource.V().next();
	}
}
