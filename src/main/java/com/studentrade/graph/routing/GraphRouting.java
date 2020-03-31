package com.studentrade.graph.routing;

import com.studentrade.graph.resource.GraphResource;
import com.studentrade.graph.server.io.Routing;
import io.javalin.Javalin;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.javalin.apibuilder.ApiBuilder.*;
import static io.javalin.apibuilder.ApiBuilder.delete;

@Singleton
public class GraphRouting extends Routing<GraphResource> {

	private Javalin javalin;

	private static final String APP_BASE_PATH = "graph";

	@Inject
	public GraphRouting(Javalin javalin) {
		this.javalin = javalin;
	}

	@Override
	public void bindRoutes() {
		javalin.routes(() -> {
			path(APP_BASE_PATH, () -> {
				path("textbooks", () -> {
					post(ctx -> getResource().getUsersWhoOwnTextbooks(ctx));
					path(":textbook", () -> {
						get(ctx -> getResource().getTextbookById(ctx));
						post(ctx -> getResource().createTextbook(ctx));
						delete(ctx -> getResource().deleteTextbook(ctx));
						path("users", () -> {
							get(ctx -> getResource().findUsersWithTextbook(ctx));
						});
					});
				});
				path("users/:user", () -> {
					path("verbs/:verb/textbooks", () -> {
						get(ctx -> getResource().getAllRelationshipsByVerb(ctx));
						path(":textbook", () -> {
							get(ctx -> getResource().getTextbookRelationship(ctx));
							post(ctx -> getResource().createTextbookRelationship(ctx));
							delete(ctx -> getResource().deleteTextbookRelationship(ctx));
						});
					});
					path("textbooks/:textbook/users/:consumer/transfer", () -> {
						post(ctx -> getResource().transferBook(ctx));
					});
					path("wishlist", () -> {
						get(ctx -> getResource().searchWishList(ctx));
					});
				});
				path("internal", () -> {
					path("users/:user", () -> {
						get(ctx -> getResource().getUser(ctx));
						post(ctx -> getResource().addUser(ctx));
						delete(ctx -> getResource().deleteUser(ctx));
					});
				});
			});
		});
	}
}
