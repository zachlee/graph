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
				path("textbook/:textbook", () -> {
					get(GraphResource::getTextbookById);
					path("add", () -> {
						post(GraphResource::createTextbook);
					});
					path("delete", () -> {
						delete(GraphResource::deleteTextbook);
					});
				});
				path("user/:user", () -> {
					path("verb/:verb/textbook/:textbook", () -> {
						get(GraphResource::getTextbookRelationship);
						post(GraphResource::createTextbookRelationship);
						delete(GraphResource::deleteTextbookRelationship);
					});
					path("textbook/:textbook/consumer/:consumer/transfer", () -> {
						post(GraphResource::transferBook);
					});
				});
				path("find", () -> {
					path("textbook/:textbook/users", () -> {
						get(GraphResource::findUsersWithTextbook);
					});
					path("textbook/users", () -> {
						post(GraphResource::getUsersWhoOwnTextbooks);
					});
					path("user/:user/wishlist", () -> {
						get(GraphResource::searchWishList);
					});
				});
				path("internal", () -> {
					path("user/:user", () -> {
						get(GraphResource::getUser);
						path("add", () -> {
							post(GraphResource::addUser);
						});
						path("delete", () -> {
							delete(GraphResource::deleteUser);
						});
					});
				});
			});
		});
	}
}
