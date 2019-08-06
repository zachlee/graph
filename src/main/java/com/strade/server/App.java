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
				path("textbook", () -> {
					path(":textbook", () -> {
						get(GraphResource::getTextbookById);
						path("add", () -> {
							post(GraphResource::addTextbook);
						});
						path("remove", () -> {
							delete(GraphResource::removeTextbook);
						});
					});
				});
				path("user/:user", () -> {
					path("verb/:verb", () -> {
						path("textbook/:textbook", () -> {
							post(GraphResource::addTextbookRelationship);
							delete(GraphResource::removeTextbookRelationship);
						});
					});
					path("textbook/:textbook", () -> {
						post(GraphResource::searchBook);
					});
					path("consumer/:consumer", () -> {
						path("transfer", () -> {
							post(GraphResource::transferBook);
						});
					});
					path("wishlist/search", () -> {
						post(GraphResource::searchWishlist);
					});
				});
				path("internal", () -> {
					path("user/:user", () -> {
						path("add", () -> {
							post(GraphResource::addUser);
						});
						path("delete", () -> {
							post(GraphResource::removeUser);
						});
					});
				});
			});
		});
	}
}
