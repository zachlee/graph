package com.studentrade.graph.auth;

import io.javalin.core.security.Role;

public enum AuthRoles implements Role {
	VERIFIED, LIMITED, INTERNAL, ADMIN, OPEN;
}
