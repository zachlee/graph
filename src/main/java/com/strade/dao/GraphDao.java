package com.strade.dao;

import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;

public class GraphDao {

	private static GraphTraversalSource graphTraversalSource;

	public GraphDao() {
		Cluster readCluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = EmptyGraph.instance().traversal().withRemote(DriverRemoteConnection.using(readCluster));
	}

	public static void testGraph() throws Exception {
		Vertex v = graphTraversalSource.V().next();
	}
}
