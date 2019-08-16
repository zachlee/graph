package com.strade.functional;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.*;
import org.apache.http.HttpHost;
import org.junit.Test;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jayway.restassured.RestAssured.given;

public class AppFunctional {
	Logger logger = Logger.getLogger(AppFunctional.class.getName());
	String localhost = "http://localhost:7000";

	public AppFunctional() {
	}

	@Test
	public void createUser() throws IOException {
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

}
