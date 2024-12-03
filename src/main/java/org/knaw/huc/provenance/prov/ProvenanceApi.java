package org.knaw.huc.provenance.prov;

import io.javalin.http.Context;
import io.javalin.http.BadRequestResponse;
import org.knaw.huc.provenance.auth.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.knaw.huc.provenance.prov.ProvenanceResource.getResourceList;
import static org.knaw.huc.provenance.util.Util.*;

public class ProvenanceApi {
    private final ProvenanceService service = new ProvenanceService();

    public void addProvenance(Context ctx) {
        Provenance provenance = getProvenanceFromRequest(ctx);

        if (provenance.source().isEmpty())
            throw new BadRequestResponse("Missing source");

        validateData(provenance);
        int id = service.createRecord(provenance);

        ctx.status(201);
        ctx.header("Location", String.format("/%s", id));
        ctx.result("Created new provenance record");
    }

    public void updateProvenance(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        if (service.getRecord(id).isEmpty())
            throw new BadRequestResponse("Invalid id: " + ctx.pathParam("id"));

        Provenance provenance = getProvenanceFromRequest(ctx);

        validateData(provenance);
        service.updateRecord(id, provenance);

        ctx.result("Updated provenance record with id " + id);
    }

    public void getProvenance(Context ctx) {
        String acceptHeader = ctx.header("Accept");
        boolean acceptsHtml = acceptHeader != null && acceptHeader.contains("text/html");
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        Optional<Provenance> provenanceInput = service.getRecord(id);
        if (provenanceInput.isEmpty())
            throw new BadRequestResponse("Invalid id: " + ctx.pathParam("id"));

        if (acceptsHtml) {
            if (provenanceInput.get().target().size() == 1) {
                String redirectLocation = provenanceInput.get().target().getFirst().resource();
                ctx.redirect("/#" + URLEncoder.encode(redirectLocation, StandardCharsets.UTF_8));
                return;
            }
            ctx.redirect("/#/target-selection/" + id);
            return;
        }
        ctx.json(provenanceInput.get());
    }

    public void getProvenanceForResource(Context ctx) {
        String resource = ctx.queryParam("resource");
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(10);
        int offset = ctx.queryParamAsClass("offset", Integer.class).getOrDefault(0);
        List<CombinedProvenance> provenanceList = service.getProvenanceForResource(resource, limit, offset);
        if (provenanceList.isEmpty())
            throw new BadRequestResponse("Invalid resource: " + resource);

        ctx.json(provenanceList);
    }

    public void getResourcesForProvenance(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).get();
        boolean isSource = ctx.queryParamAsClass("is_source", Boolean.class).get();
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(10);
        int offset = ctx.queryParamAsClass("offset", Integer.class).getOrDefault(0);
        List<ProvenanceResource> resources = service.getResourcesForProvenance(id, isSource, limit, offset);
        if (resources.isEmpty())
            throw new BadRequestResponse("Invalid id: " + ctx.pathParam("id"));

        ctx.json(resources);
    }

    public void getProvenanceTemplates(Context ctx) {
        List<ProvenanceTemplate> provenanceTemplates = service.getProvenanceTemplates();
        ctx.json(provenanceTemplates);
    }

    private static Provenance getProvenanceFromRequest(Context ctx) {
        String who = ctx.formParam("who");
        if (who == null && ctx.attribute("user") instanceof User user)
            who = user.who();

        List<ProvenanceResource> source = getResourceList(
                ctx.formParams("source"), ctx.formParams("source_rel"));
        List<ProvenanceResource> target = getResourceList(
                ctx.formParams("target"), ctx.formParams("target_rel"));

        return Provenance.create(
                0,
                who,
                ctx.formParam("where"),
                ctx.formParam("when"),
                ctx.formParam("how"),
                ctx.formParam("why"),
                ctx.formParam("how_software"),
                ctx.formParam("how_init"),
                ctx.formParam("how_delta"),
                ctx.formParam("why_motivation"),
                ctx.formParam("why_provenance_schema"),
                source, target);
    }

    private static void validateData(Provenance provenance) {
        if (provenance.who() != null && !isValidUri(provenance.who()))
            throw new BadRequestResponse("Invalid URI for 'who'");

        if (provenance.where() != null && !isValidUri(provenance.where()))
            throw new BadRequestResponse("Invalid URI for 'where'");

        if (provenance.when() != null && !isValidUri(provenance.when())
            && !isValidTimestamp(provenance.when()))
            throw new BadRequestResponse("Invalid URI or timestamp for 'when'");

        if (provenance.howSoftware() != null && !isValidUri(provenance.howSoftware()))
            throw new BadRequestResponse("Invalid URI 'how_software'");

        if (provenance.howDelta() != null &&
            !provenance.howDelta().trim().startsWith("+") &&
            !provenance.howDelta().trim().startsWith("-"))
            throw new BadRequestResponse("Invalid delta for 'how_delta'");
    }
}
