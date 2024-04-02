package org.knaw.huc.provenance.auth;

import io.javalin.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

public class AuthApi {
    private final AuthService service = new AuthService();

    public enum Role implements RouteRole {
        ANONYMOUS, USER
    }

    public void beforeMatched(Context ctx) {
        if (ctx.routeRoles().isEmpty())
            return;

        String auth = ctx.header("Authorization");
        if (auth != null)
            auth = auth.replaceFirst("^Basic:", "");

        User user = (auth != null) ? service.getUserById(auth.trim()) : null;
        ctx.attribute("user", user);

        RouteRole role = (user != null) ? Role.USER : Role.ANONYMOUS;
        if (!ctx.routeRoles().contains(role))
            throw new UnauthorizedResponse();
    }
}
