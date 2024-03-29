package com.studentrade.graph.dao;

import java.util.*;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.studentrade.graph.domain.Relationship;
import com.studentrade.graph.domain.Textbook;
import com.studentrade.graph.domain.User;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.driver.ser.Serializers;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static com.studentrade.graph.util.Labels.*;

public class TextbookGraphDaoImpl implements TextbookGraphDao {

	private static GraphTraversalSource graphTraversalSource;

	private static final DynamicStringProperty GREMLIN_DOMAIN = DynamicPropertyFactory.getInstance()
			.getStringProperty("gremlin.server.domain", "");

	public TextbookGraphDaoImpl() {
		Cluster cluster = Cluster.build()
				.port(8182)
				.addContactPoint(GREMLIN_DOMAIN.get())
				.serializer(Serializers.GRAPHSON_V3D0)
				.create();

		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
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

	public boolean isVerbValid(String verb) {
		return verb.equals(OWNS_VERB) || verb.equals(WANTS_VERB);
	}

	public Textbook getTextbook(String textbookId) {
		GraphTraversal<Vertex, Map<Object, Object>> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.limit(1)
				.valueMap(true);
		if (traversal.hasNext()) {
			Map<Object, Object> textbookValueMap = traversal.next();
			return createTextbookFromMap(textbookValueMap);
		} else {
			return null;
		}
	}

	public Textbook getTextbookIsbn10(String isbn10) {
		GraphTraversal<Vertex, Map<Object, Object>> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(ISBN10, isbn10)
				.limit(1)
				.valueMap(true);
		if (traversal.hasNext()) {
			Map<Object, Object> textbookValueMap = traversal.next();
			return createTextbookFromMap(textbookValueMap);
		} else {
			return null;
		}
	}

	public Textbook getTextbookIsbn13(String isbn13) {
		GraphTraversal<Vertex, Map<Object, Object>> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(ISBN13, isbn13)
				.limit(1)
				.valueMap(true);
		if (traversal.hasNext()) {
			Map<Object, Object> textbookValueMap = traversal.next();
			return createTextbookFromMap(textbookValueMap);
		} else {
			return null;
		}
	}

	public boolean createTextbook(Textbook textbook) {
		GraphTraversal<Vertex, Vertex> traversal = graphTraversalSource.addV(TEXTBOOK_LABEL)
				.property(NODE_UUID, textbook.getUuid())
				.property(TITLE, textbook.getTitle())
				.property(AUTHOR, textbook.getAuthor())
				.property(ISBN10, textbook.getIsbn10())
				.property(ISBN13, textbook.getIsbn13())
				.property(IMAGE_LINK, textbook.getImageLink());
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

	public User getUser(String userId) {
		GraphTraversal<Vertex, Map<Object, Object>> getUserTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1)
				.valueMap(true);
		if (getUserTraversal.hasNext()) {
			Map<Object, Object> user = getUserTraversal.next();
			return createUserFromMap(user);
		} else {
			return null;
		}
	}

	public boolean doesUserExist(String userId) {
		GraphTraversal<Vertex, Vertex> getUserTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has("uuid", userId)
				.limit(1);
		return getUserTraversal.hasNext();
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
		GraphTraversal<Vertex, Path> relationshipTraversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.outE(verb)
				.as(RELATIONSHIP_ALIAS)
				.inV()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.select(RELATIONSHIP_ALIAS)
				.path()
				.by(__.valueMap(true));
		if (relationshipTraversal.hasNext()) {
			Path relationshipMap = relationshipTraversal.next();
			return createRelationshipFromPath(relationshipMap);
		} else {
			return null;
		}
	}

	public List<User> getUsersWhoOwnTextbook(String textbookId) {
		GraphTraversal<Vertex, List<Map<Object, Object>>> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, textbookId)
				.in(OWNS_VERB)
				.hasLabel(USER_LABEL)
				.valueMap(true)
				.fold();
		List<User> userList = new ArrayList<>();
		if (traversal.hasNext()) {
			List<Map<Object, Object>> traversalList = traversal.next();
			for (Map<Object, Object> userValueMap : traversalList) {
				User user = createUserFromMap(userValueMap);
				userList.add(user);
			}
			return userList;
		} else {
			return null;
		}
	}

	public Map<Long, List<User>> getUsersWhoOwnTextbooks(List<String> textbookIds) {
		GraphTraversal<Vertex, List<Map<Object, Long>>> traversal = graphTraversalSource.V()
				.hasLabel(TEXTBOOK_LABEL)
				.has(NODE_UUID, P.within(textbookIds))
				.in(OWNS_VERB)
				.hasLabel(USER_LABEL)
				.valueMap()
				.groupCount()
				.order()
				.fold();
		if (traversal.hasNext()) {
			Map<Object, Long> traversalMap = traversal.next().get(0);
			Map<Long, List<User>> orderedUserMap = extractOrderedUserMapFromTraversal(traversalMap);
			return orderedUserMap;
		} else {
			return null;
		}
	}

	public Map<Long, List<User>> getUsersWhoOwnWantedTextbooks(String userId) {
		GraphTraversal<Vertex, List<Map<Object, Long>>> traversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.out(WANTS_VERB)
				.hasLabel(TEXTBOOK_LABEL)
				.in(OWNS_VERB)
				.hasLabel(USER_LABEL)
				.valueMap()
				.groupCount()
				.order()
				.fold();
		if (traversal.hasNext()) {
			Map<Object, Long> traversalMap = traversal.next().get(0);
			Map<Long, List<User>> orderedUserMap = extractOrderedUserMapFromTraversal(traversalMap);
			return orderedUserMap;
		} else {
			return null;
		}
	}

	public List<Textbook> getTextbooksByVerb(String userId, String verb) {
		GraphTraversal<Vertex, List<Map<Object, Object>>> traversal = graphTraversalSource.V()
				.hasLabel(USER_LABEL)
				.has(NODE_UUID, userId)
				.out(verb)
				.hasLabel(TEXTBOOK_LABEL)
				.valueMap(true)
				.fold();
		if (traversal.hasNext()) {
			List<Textbook> textbooks = new ArrayList<>();
			List<Map<Object, Object>> textbooksValueMapList = traversal.next();
			for (Map<Object, Object> textbookValueMap : textbooksValueMapList) {
				Textbook textbookFromValueMap = createTextbookFromMap(textbookValueMap);
				textbooks.add(textbookFromValueMap);
			}
			return textbooks;
		} else {
			return null;
		}
	}

	private Map<Long, List<User>> extractOrderedUserMapFromTraversal(Map<Object, Long> objectMap) {
		Set<Map.Entry<Object, Long>> returnedUserMapSet = objectMap.entrySet();
		Iterator<Map.Entry<Object, Long>> iterator = returnedUserMapSet.iterator();
		Map<Long, List<User>> unOrderedUserMap = new HashMap<>();
		while (iterator.hasNext()) {
			Map.Entry<Object, Long> entry = iterator.next();
			iterateAndAddUserToMap(unOrderedUserMap, entry);
		}
		LinkedHashMap<Long, List<User>> orderedUserMap = new LinkedHashMap<>();

		unOrderedUserMap.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
				.forEachOrdered(x -> orderedUserMap.put(x.getKey(), x.getValue()));

		return orderedUserMap;
	}

	private void iterateAndAddUserToMap(Map<Long, List<User>> orderedUserMap, Map.Entry<Object, Long> entry) {
		Object userMap = entry.getKey();
		User user = createUserFromMap((Map<Object, Object>) userMap);
		Long numberOfRelationships = entry.getValue();
		buildOrderedUserMap(orderedUserMap, user, numberOfRelationships);
	}

	private void buildOrderedUserMap(Map<Long, List<User>> orderedUserMap, User user, Long numberOfRelationships) {
		if (!orderedUserMap.containsKey(numberOfRelationships)) {
			orderedUserMap.put(numberOfRelationships, new ArrayList<>());
			orderedUserMap.get(numberOfRelationships).add(user);
		} else {
			orderedUserMap.get(numberOfRelationships).add(user);
		}
	}

	private Textbook createTextbookFromMap(Map<Object, Object> textbookValueMap) {
		return new Textbook(getString(textbookValueMap.get(NODE_UUID)),
				getString(textbookValueMap.get(TITLE)),
				getString(textbookValueMap.get(AUTHOR)),
				getString(textbookValueMap.get(ISBN10)),
				getString(textbookValueMap.get(ISBN13)),
				getString(textbookValueMap.get(IMAGE_LINK)));
	}

	private Relationship createRelationshipFromPath(Path relationshipPath) {
		List<Object> pathObjects = relationshipPath.objects();
		Map<String, String> relationshipMap = parsePathObjectsIntoRelationshipMap(pathObjects);
		return new Relationship(relationshipMap.get("userId"),
				relationshipMap.get("verbLabel"),
				relationshipMap.get("textbookId"));
	}

	private Map<String, String> parsePathObjectsIntoRelationshipMap(List<Object> pathObjects) {
		String userId = extractIdFromPathObject(pathObjects.get(0));
		String verbLabel = extractEdgeLabelFromPathObject(pathObjects.get(1));
		String textbookId = extractIdFromPathObject(pathObjects.get(2));

		Map<String, String> relationshipMap = new HashMap<>();
		relationshipMap.put("userId", userId);
		relationshipMap.put("verbLabel", verbLabel);
		relationshipMap.put("textbookId", textbookId);

		return relationshipMap;
	}

	private String extractIdFromPathObject(Object vertexObjectFromPath) {
		Map<String, List<String>> valueMap = (Map<String, List<String>>) vertexObjectFromPath;
		List<String> uuidList = valueMap.get("uuid");
		return uuidList.get(0);
	}

	private String extractEdgeLabelFromPathObject(Object edgeObjectFromPath) {
		HashMap<T, String> edgeMap = (HashMap<T, String>) edgeObjectFromPath;
		boolean labelFound = false;
		Iterator<Map.Entry<T, String>> iterator = edgeMap.entrySet().iterator();
		String verbLabel = "";
		while (!labelFound && iterator.hasNext()) {
			Object value = iterator.next().getValue();
			if (value instanceof String) {
				labelFound = true;
				verbLabel = (String) value;
			}
		}
		return verbLabel;
	}

	private User createUserFromMap(Map<Object, Object> userValueMap) {
		return new User(getString(userValueMap.get(NODE_UUID)),
				getString(userValueMap.get(USERNAME)),
				getString(userValueMap.get(EMAIL)),
				getString(userValueMap.get(SCHOOL)),
				getString(userValueMap.get(TYPE)));
	}

	private String getString(Object entry) {
		List<String> list = (LinkedList<String>) entry;
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		} else {
			return "";
		}
	}
}
