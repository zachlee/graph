package com.strade.server;

import com.strade.resource.GraphResource;
import io.javalin.Javalin;

import java.util.logging.Logger;

import static io.javalin.apibuilder.ApiBuilder.*;

public class App {
	Logger logger = Logger.getLogger(App.class.getName());

	public static void main(String[] args) {
		Javalin app = Javalin
				.create()
				//todo this should read from a properties file based on environment
				.enableCorsForOrigin("http://localhost:8080")
				.start(7000);
		app.routes(() -> {
			path("about", () -> {
				get(GraphResource::aboutPage);
			});
			path("graph", () -> {
				path("textbooks", () -> {
					path(":textbook", () -> {
						get(GraphResource::getTextbookById);
						post(GraphResource::createTextbook);
						delete(GraphResource::deleteTextbook);
						path("users", () -> {
							get(GraphResource::findUsersWithTextbook);
						});
					});
					path("users", () -> {
						post(GraphResource::getUsersWhoOwnTextbooks);
					});
				});
				path("users/:user", () -> {
					path("verbs/:verb/textbooks/:textbook", () -> {
						get(GraphResource::getTextbookRelationship);
						post(GraphResource::createTextbookRelationship);
						delete(GraphResource::deleteTextbookRelationship);
					});
					path("textbooks/:textbook/users/:consumer/transfer", () -> {
						post(GraphResource::transferBook);
					});
					path("wishlist", () -> {
						get(GraphResource::searchWishList);
					});
				});
				path("internal", () -> {
					path("users/:user", () -> {
						get(GraphResource::getUser);
						post(GraphResource::addUser);
						delete(GraphResource::deleteUser);
					});
				});
			});
		});
	}
}
