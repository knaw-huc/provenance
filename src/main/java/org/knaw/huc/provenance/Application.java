package org.knaw.huc.provenance;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;
import org.jdbi.v3.core.JdbiException;
import org.knaw.huc.provenance.auth.AuthApi;
import org.knaw.huc.provenance.prov.ProvenanceApi;
import org.knaw.huc.provenance.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        final AuthApi authApi = new AuthApi();

        final Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> mapper
                    .registerModule(new JavaTimeModule())
                    .setDateFormat(new StdDateFormat())));
            config.staticFiles.add("/static", Location.CLASSPATH);
            config.router.apiBuilder(Application::routes);
        }).start(Config.PORT);

        app.beforeMatched(authApi::beforeMatched);
        app.exception(JdbiException.class, (e, ctx) ->
                LOGGER.error("Database error with form params: " + ctx.formParamMap(), e));
    }

    private static void routes() {
        final ProvenanceApi provenanceApi = new ProvenanceApi();

        path("/prov", () -> {
            post(provenanceApi::addProvenance, AuthApi.Role.USER);

            path("/{id}", () -> {
                put(provenanceApi::updateProvenance, AuthApi.Role.USER);
                get(provenanceApi::getProvenance, AuthApi.Role.ANONYMOUS);
            });
        });

        get("/trail", provenanceApi::getProvenanceTrail, AuthApi.Role.ANONYMOUS);
    }
}
