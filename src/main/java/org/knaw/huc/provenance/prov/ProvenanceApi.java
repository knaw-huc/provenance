package org.knaw.huc.provenance.prov;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import org.knaw.huc.provenance.auth.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static org.knaw.huc.provenance.util.Util.isValidUri;

public class ProvenanceApi {
    private final ProvenanceService service = new ProvenanceService();

    public void addProvenance(Context ctx) {
        ProvenanceInput provenanceInput = getProvenanceInputFromRequest(ctx);

        if (provenanceInput.source().isEmpty())
            throw new BadRequestResponse("Missing source");

        validateUris(provenanceInput);
        int id = service.createRecord(provenanceInput);

        ctx.status(201);
        ctx.header("Location", String.format("/%s", id));
        ctx.result("Created new provenance record");
    }

    public void updateProvenance(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (!service.recordExists(id))
            throw new BadRequestResponse("Invalid id: " + ctx.pathParam("id"));

        ProvenanceInput provenanceInput = getProvenanceInputFromRequest(ctx);

        validateUris(provenanceInput);
        service.updateRecord(id, provenanceInput);

        ctx.result("Updated provenance record with id " + id);
    }

    private static ProvenanceInput getProvenanceInputFromRequest(Context ctx) {
        String who = ctx.formParam("who");
        if (who == null && ctx.attribute("user") instanceof User user)
            who = user.who();

        List<ProvenanceInput.ProvenanceResourceInput> source = getProvenanceResourceInputs(
                ctx.formParams("source"), ctx.formParams("source_rel"));
        List<ProvenanceInput.ProvenanceResourceInput> target = getProvenanceResourceInputs(
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

    private static List<ProvenanceInput.ProvenanceResourceInput> getProvenanceResourceInputs(
            List<String> resources, List<String> relations) {
        if (resources == null || relations == null)
            return new ArrayList<>();

        return IntStream.range(0, Math.min(resources.size(), relations.size()))
                .mapToObj(i -> new ProvenanceInput.ProvenanceResourceInput(resources.get(i), relations.get(i)))
                .collect(Collectors.toList());
    }

    private static void validateUris(ProvenanceInput provenanceInput) {
        if (provenanceInput.who() != null && !isValidUri(provenanceInput.who()))
            throw new BadRequestResponse("Invalid URI for 'who'");

        if (provenanceInput.where() != null && !isValidUri(provenanceInput.where()))
            throw new BadRequestResponse("Invalid URI for 'where'");

        if (provenanceInput.when() != null && !isValidUri(provenanceInput.when()))
            throw new BadRequestResponse("Invalid URI for 'when'");

        if (provenanceInput.how() != null && !isValidUri(provenanceInput.how()))
            throw new BadRequestResponse("Invalid URI for 'how'");
    }
}
