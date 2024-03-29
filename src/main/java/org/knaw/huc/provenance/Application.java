package org.knaw.huc.provenance;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new StdDateFormat());

        final AuthApi authApi = new AuthApi();
        final ProvenanceApi provenanceApi = new ProvenanceApi();

        final Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.accessManager(authApi::manage);
            config.jsonMapper(new JavalinJackson(objectMapper));
            config.staticFiles.add("/static", Location.CLASSPATH);
        }).start(Config.PORT);

        app.routes(() -> {
            post("/prov", provenanceApi::addProvenance, AuthApi.Role.USER);
            put("/prov/{id}", provenanceApi::updateProvenance, AuthApi.Role.USER);
            get("/prov/{id}", provenanceApi::getProvenance, AuthApi.Role.ANONYMOUS);
            get("/trail", provenanceApi::getProvenanceTrail, AuthApi.Role.ANONYMOUS);
        });

        app.exception(JdbiException.class, (e, ctx) ->
                LOGGER.error("Database error with form params: " + ctx.formParamMap(), e));
    }
}
