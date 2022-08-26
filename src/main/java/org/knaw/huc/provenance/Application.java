package org.knaw.huc.provenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.json.JavalinJackson;
import org.knaw.huc.provenance.auth.AuthApi;
import org.knaw.huc.provenance.prov.ProvenanceApi;
import org.knaw.huc.provenance.util.Config;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Application {
    public static void main(String[] args) {
        final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new StdDateFormat());

        final AuthApi authApi = new AuthApi();
        final ProvenanceApi provenanceApi = new ProvenanceApi();

        final Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.enableDevLogging();
            config.accessManager(authApi::manage);
            config.jsonMapper(new JavalinJackson(objectMapper));
            config.addStaticFiles("/static", Location.CLASSPATH);
        }).start(Config.PORT);

        app.routes(() -> {
            post("/prov", provenanceApi::addProvenance, AuthApi.Role.USER);
            put("/prov/{id}", provenanceApi::updateProvenance, AuthApi.Role.USER);
            get("/prov/{id}", provenanceApi::getProvenance, AuthApi.Role.ANONYMOUS);
            get("/trail", provenanceApi::getProvenanceTrail, AuthApi.Role.ANONYMOUS);
        });
    }
}
