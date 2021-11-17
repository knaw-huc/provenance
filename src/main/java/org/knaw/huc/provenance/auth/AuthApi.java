package org.knaw.huc.provenance.auth;

import io.javalin.core.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

import java.util.Set;

public class AuthApi {
    private final AuthService service = new AuthService();

    public enum Role implements RouteRole {
        ANONYMOUS, USER
    }

    public void manage(Handler handler, Context ctx, Set<RouteRole> routeRoles) throws Exception {
        String auth = ctx.header("Authorization");
        if (auth != null)
            auth = auth.replaceFirst("^Basic:", "");

        User user = (auth != null) ? service.getUserById(auth.trim()) : null;
        ctx.attribute("user", user);

        RouteRole role = (user != null) ? Role.USER : Role.ANONYMOUS;
        if (!routeRoles.contains(role))
            throw new UnauthorizedResponse();

        handler.handle(ctx);
    }
}
