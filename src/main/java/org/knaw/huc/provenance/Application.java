package org.knaw.huc.provenance;

import static spark.Spark.*;

public class Application {
    public static void main(String[] args) {
        port(Config.PORT);

        ProvenanceApi provenanceApi = new ProvenanceApi();
        post("/", provenanceApi::addProvenance);
        put("/:id", provenanceApi::updateProvenance);
    }
}
