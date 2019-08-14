package com.strade.server;

import com.strade.resource.GraphResource;
import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;

public class App {
	public static void main(String[] args) {
		Javalin app = Javalin
				.create()
				//todo this should read from a properties file based on environment
				.enableCorsForOrigin("http://localhost:8080")
				.start(7000);
		app.routes(()-> {
			path("/", () -> {
				post(GraphResource::aboutPage);
			});
			path("graph", () -> {
				path("textbook/:textbook", () -> {
					get(GraphResource::getTextbookById);
					path("add", () -> {
						post(GraphResource::createTextbook);
					});
					path("remove", () -> {
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
					path("textbook/:textbook", () -> {
						get(GraphResource::findUsersWithTextbook);
					});
					path("textbook/users", () -> {
						post(GraphResource::getUsersWhoOwnTextbooks);
					});
					path("user/:user", () -> {
						post(GraphResource::searchWishList);
					});
				});
				path("internal", () -> {
					path("user/:user", () -> {
						get(GraphResource::getUser);
						path("add", () -> {
							post(GraphResource::addUser);
						});
						path("delete", () -> {
							post(GraphResource::deleteUser);
						});
					});
				});
			});
		});
	}
}
