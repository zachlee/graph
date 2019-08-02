package com.strade.server;

import com.strade.service.GraphService;
import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class App {
	public static void main(String[] args) {
		Javalin app = Javalin
				.create()
				//todo this should read from a properties file based on environment
				.enableCorsForOrigin("http://localhost:8080")
				.start(7000);
		app.routes(()-> {
			path("/", () -> {
				post(GraphService::aboutPage);
			});
			path("graph", () -> {
				path("textbook", () -> {
					path(":textbook", () -> {
						path("add", () -> {
							post(GraphService::addTextbook);
						});
						path("remove", () -> {
							delete(GraphService::removeTextbook);
						});
					});
				});
				path("user/:user", () -> {
					path("verb/:verb", () -> {
						path("textbook/:textbook", () -> {
							post(GraphService::addTextbookRelationship);
						});
					});
					path("textbook/:textbook", () -> {
						post(GraphService::searchBook);
					});
					path("consumer/:consumer", () -> {
						path("transfer", () -> {
							post(GraphService::transferBook);
						});
					});
					path("wishlist/search", () -> {
						post(GraphService::searchWishlist);
					});
				});
				path("internal", () -> {
					path("user/:user", () -> {
						path("add", () -> {
							post(GraphService::addUser);
						});
						path("delete", () -> {
							post(GraphService::removeUser);
						});
					});
				});
			});
		});
	}
}
