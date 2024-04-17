package org.knaw.huc.provenance;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.jdbi.v3.core.JdbiException;
import org.knaw.huc.provenance.auth.AuthApi;
import org.knaw.huc.provenance.prov.ProvenanceApi;
import org.knaw.huc.provenance.trail.TrailApi;
import org.knaw.huc.provenance.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.*;
import static org.knaw.huc.provenance.auth.AuthApi.Role.*;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        final Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.useVirtualThreads = true;
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> mapper
                    .registerModule(new JavaTimeModule())
                    .setDateFormat(new StdDateFormat())));
            config.staticFiles.add("/static");
            config.spaRoot.addFile("/", "/static");
            config.router.apiBuilder(Application::routes);
        }).start(Config.PORT);

        final AuthApi authApi = new AuthApi();
        app.beforeMatched(authApi::beforeMatched);

        app.exception(JdbiException.class, (e, ctx) ->
                LOGGER.error("Database error with form params: {}", ctx.formParamMap(), e));
    }

    private static void routes() {
        path("/prov", () -> {
            final ProvenanceApi provenanceApi = new ProvenanceApi();

            get(provenanceApi::getProvenanceForResource, ANONYMOUS);
            post(provenanceApi::addProvenance, USER);

            get("/templates", provenanceApi::getProvenanceTemplates, ANONYMOUS);

            path("/{id}", () -> {
                get(provenanceApi::getProvenance, ANONYMOUS);
                put(provenanceApi::updateProvenance, USER);

                get("/resources", provenanceApi::getResourcesForProvenance, ANONYMOUS);
            });
        });

        path("/trail", () -> {
            final TrailApi trailApi = new TrailApi();
            get(trailApi::getProvenanceTrail, ANONYMOUS);
        });
    }
}
