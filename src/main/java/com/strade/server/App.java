package com.strade.server;

import com.strade.resource.GraphResource;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;

import static io.javalin.apibuilder.ApiBuilder.*;

public class App {

	public static void main(String[] args) {
		Javalin app = Javalin.create(JavalinConfig::enableCorsForAllOrigins);
		app.start(7000);

		app.routes(() -> {
			path("graph", () -> {
				path("about", () -> {
					get(GraphResource::aboutPage);
				});
				path("textbooks", () -> {
					post(GraphResource::getUsersWhoOwnTextbooks);
					path(":textbook", () -> {
						get(GraphResource::getTextbookById);
						post(GraphResource::createTextbook);
						delete(GraphResource::deleteTextbook);
						path("users", () -> {
							get(GraphResource::findUsersWithTextbook);
						});
					});
				});
				path("users/:user", () -> {
					path("verbs/:verb/textbooks", () -> {
						get(GraphResource::getAllRelationshipsByVerb);
						path(":textbook", () -> {
							get(GraphResource::getTextbookRelationship);
							post(GraphResource::createTextbookRelationship);
							delete(GraphResource::deleteTextbookRelationship);
						});
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
