package com.strade.functional;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.strade.domain.User;
import com.strade.util.TestUtils;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jayway.restassured.RestAssured.given;
import static com.strade.util.TestUtils.*;

public class AppFunctional {
	private Logger logger = Logger.getLogger(AppFunctional.class.getName());
	private String localhost = "http://localhost:7000";

	public AppFunctional() {
	}

	private static GraphTraversalSource graphTraversalSource;

	@BeforeClass
	public static void setup() {
		Cluster cluster = Cluster.build().port(8182).addContactPoint("localhost").create();
		graphTraversalSource = AnonymousTraversalSource.traversal().withRemote(DriverRemoteConnection.using(cluster));
	}

	@Test
	public void aboutPage() throws IOException {
		Response response = given()
				.log()
				.everything()
				.and()
				.contentType(ContentType.JSON)
				.when()
				.get(localhost + "/about");

		response.then().log().everything().and().assertThat().contentType(ContentType.JSON);
		String as = response.getBody().as(String.class);
		logger.log(Level.INFO, as);
	}

	@Test
	public void getUser() {
		String userId = UUID.randomUUID().toString();
		User user = createUser(userId);
		createUserTraversalAndAssert(user, graphTraversalSource);
		try {
			Response response = given()
					.log()
					.everything()
					.and()
					.contentType(ContentType.JSON)
					.when()
					.get(String.format(localhost + "/graph/internal/user/%s", userId));

			User returnedUser = response.getBody().as(User.class);
			assert null != returnedUser;
		} finally {
			graphTraversalSource.V().has("uuid", userId).drop().iterate();
		}
	}

	@Test
	public void getNonExistantUserReturns404

}
