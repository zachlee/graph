package com.strade.functional;

import com.strade.dao.GraphDao;
import com.strade.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static com.strade.utils.Labels.*;
import static com.strade.utils.Labels.SCHOOL;
import static com.strade.utils.Labels.TYPE;

public class FunctionalDao {

	GraphDao graphDao = GraphDao.getInstance();
	private static GraphTraversalSource graphTraversalSource;

	@BeforeClass
	public static void setup() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	@Test
	public void createUserTest() {
		String userId = UUID.randomUUID().toString();
		try {
			User user = createUser(userId);
			boolean addUser = graphDao.createUser(user);
			assert addUser;

			GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
					.hasLabel(USER_LABEL)
					.has("uuid", userId)
					.limit(1)
					.valueMap(true);
			Map<Object, Object> userMap = getUserTraversal.next();
			User returnedUser = createUserFromMap(userMap);
			assert null != returnedUser;
			assert returnedUser.getUuid().equals(userId);
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getUserReturnsUser() {
		String userId = UUID.randomUUID().toString();
		try {
			User user = createUser(userId);
			GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(USER_LABEL)
					.property(NODE_UUID, user.getUuid())
					.property(SCHOOL, user.getSchool())
					.property(EMAIL, user.getEmail())
					.property(USERNAME, user.getUsername())
					.property(TYPE, user.getType());
			traversal.hasNext();

			User daoUser = graphDao.getUser(userId);
			assert null != daoUser;
			assert daoUser.getUuid().equals(userId);
			assert daoUser.getType().equals(user.getType());
			assert daoUser.getUsername().equals(user.getUsername());
		} finally {
			graphTraversalSource.V().hasLabel(USER_LABEL).has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getNonExistingUserReturnsNull() {
		User nonExistentUser = graphDao.getUser("DoesntExist");
		assert null == nonExistentUser;
	}

	@Test
	public void deleteUserTest() {
		String userId = UUID.randomUUID().toString();
		User user = createUser(userId);
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(USER_LABEL)
				.property(NODE_UUID, user.getUuid())
				.property(SCHOOL, user.getSchool())
				.property(EMAIL, user.getEmail())
				.property(USERNAME, user.getUsername())
				.property(TYPE, user.getType());
		traversal.hasNext();

		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		Map<Object, Object> userMap = getUserTraversal.next();
		User getUser = createUserFromMap(userMap);

		assert null != getUser;

		graphDao.deleteUser(userId);

		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversalAfterDelete = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		boolean userExists = getUserTraversal.hasNext();
		assert !userExists;
	}



	private User createUser(String uuid) {
		return new User(uuid,
				"username",
				"email",
				"school",
				"type");
	}

	private User createUserFromMap(Map<Object, Object> userValueMap) {
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
