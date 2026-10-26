package app.security;

import io.javalin.security.RouteRole;

public enum AccessRole implements RouteRole {
    ANYONE, USER, ADMIN
}
