package org.knaw.huc.provenance;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.knaw.huc.provenance.auth.AuthApi;
import org.knaw.huc.provenance.prov.ProvenanceApi;
import org.knaw.huc.provenance.util.Config;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Application {
    public static void main(String[] args) {
        final AuthApi authApi = new AuthApi();
        final ProvenanceApi provenanceApi = new ProvenanceApi();

        final Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.enableDevLogging();
            config.accessManager(authApi::manage);
            config.addStaticFiles("/static", Location.CLASSPATH);
        }).start(Config.PORT);

        app.routes(() -> {
            post("/api", provenanceApi::addProvenance, AuthApi.Role.USER);
            put("/api/{id}", provenanceApi::updateProvenance, AuthApi.Role.USER);
            get("/api/{id}", provenanceApi::getProvenance, AuthApi.Role.ANONYMOUS);
            get("/api/{id}/trail", provenanceApi::getProvenanceTrail, AuthApi.Role.ANONYMOUS);
        });
    }
}
