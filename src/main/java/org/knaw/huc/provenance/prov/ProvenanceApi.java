package org.knaw.huc.provenance.prov;

import io.javalin.http.Context;
import io.javalin.http.BadRequestResponse;
import org.knaw.huc.provenance.auth.User;

import java.util.List;
import java.util.Optional;

import static org.knaw.huc.provenance.util.Util.*;
import static org.knaw.huc.provenance.prov.ProvenanceInput.ProvenanceResourceInput.getInputList;

public class ProvenanceApi {
    private final ProvenanceService service = new ProvenanceService();

    public void addProvenance(Context ctx) {
        ProvenanceInput provenanceInput = getProvenanceInputFromRequest(ctx);

        if (provenanceInput.source().isEmpty())
            throw new BadRequestResponse("Missing source");

        validateData(provenanceInput);
        int id = service.createRecord(provenanceInput);

        ctx.status(201);
        ctx.header("Location", String.format("/%s", id));
        ctx.result("Created new provenance record");
    }

    public void updateProvenance(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (service.getRecord(id).isEmpty())
            throw new BadRequestResponse("Invalid id: " + ctx.pathParam("id"));

        ProvenanceInput provenanceInput = getProvenanceInputFromRequest(ctx);

        validateData(provenanceInput);
        service.updateRecord(id, provenanceInput);

        ctx.result("Updated provenance record with id " + id);
    }

    public void getProvenance(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        Optional<ProvenanceInput> provenanceInput = service.getRecord(id);
        if (provenanceInput.isEmpty())
            throw new BadRequestResponse("Invalid id: " + ctx.pathParam("id"));

        ctx.json(provenanceInput.get());
    }

    public void getProvenanceTrail(Context ctx) {
        String resource = ctx.queryParam("resource");
        if (resource == null)
            throw new BadRequestResponse("No resource given");

        ProvenanceTrail provenanceTrail = service.getTrail(resource.trim());
        if (provenanceTrail == null)
            throw new BadRequestResponse("Invalid resource: " + resource.trim());

        ctx.json(provenanceTrail);
    }

    private static ProvenanceInput getProvenanceInputFromRequest(Context ctx) {
        String who = ctx.formParam("who");
        if (who == null && ctx.attribute("user") instanceof User user)
            who = user.who();

        List<ProvenanceInput.ProvenanceResourceInput> source = getInputList(
                ctx.formParams("source"), ctx.formParams("source_rel"));
        List<ProvenanceInput.ProvenanceResourceInput> target = getInputList(
                ctx.formParams("target"), ctx.formParams("target_rel"));

        return new ProvenanceInput(
                source,
                target,
                who,
                ctx.formParam("where"),
                ctx.formParam("when"),
                ctx.formParam("how"),
                ctx.formParam("why"));
    }

    private static void validateData(ProvenanceInput provenanceInput) {
        if (provenanceInput.who() != null && !isValidUri(provenanceInput.who()))
            throw new BadRequestResponse("Invalid URI for 'who'");

        if (provenanceInput.where() != null && !isValidUri(provenanceInput.where()))
            throw new BadRequestResponse("Invalid URI for 'where'");

        if (provenanceInput.when() != null && !isValidUri(provenanceInput.when())
                && !isValidTimestamp(provenanceInput.when()))
            throw new BadRequestResponse("Invalid URI or timestamp for 'when'");

        if (provenanceInput.how() != null && !isValidUri(provenanceInput.how())
                && !provenanceInput.how().trim().startsWith("+") && !provenanceInput.how().trim().startsWith("-"))
            throw new BadRequestResponse("Invalid URI or delta for 'how'");
    }
}
