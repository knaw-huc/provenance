package org.knaw.huc.provenance;

import io.javalin.Javalin;
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
        }).start(Config.PORT);

        app.routes(() -> {
            post("/", provenanceApi::addProvenance, AuthApi.Role.USER);
            put("/{id}", provenanceApi::updateProvenance, AuthApi.Role.USER);
        });
    }
}
