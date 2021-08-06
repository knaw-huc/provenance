package org.knaw.huc.provenance;

import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

import static org.knaw.huc.provenance.Util.isValidUri;

public class ProvenanceApi {
    private final ProvenanceService service = new ProvenanceService();

    public String addProvenance(Request request, Response response) {
        ProvenanceInput provenanceInput = getProvenanceInputFromRequest(request);

        if (provenanceInput.getSource().isEmpty()) {
            response.status(400);
            return "Missing source";
        }

        String uriValidationError = validateUris(provenanceInput);
        if (uriValidationError != null) {
            response.status(400);
            return uriValidationError;
        }

        int id = service.createRecord(provenanceInput);

        response.status(201);
        response.header("Location", String.format("/%s", id));

        return "Created new provenance record";
    }

    public String updateProvenance(Request request, Response response) {
        try {
            int id = Integer.parseInt(request.params("id"));
            if (!service.recordExists(id)) {
                response.status(400);
                return "Invalid id: " + request.params("id");
            }

            ProvenanceInput provenanceInput = getProvenanceInputFromRequest(request);

            String uriValidationError = validateUris(provenanceInput);
            if (uriValidationError != null) {
                response.status(400);
                return uriValidationError;
            }

            service.updateRecord(id, provenanceInput);

            return "Updated provenance record with id " + id;
        } catch (NumberFormatException nfe) {
            response.status(400);
            return "Invalid id: " + request.params("id");
        }
    }

    private static ProvenanceInput getProvenanceInputFromRequest(Request request) {
        List<ProvenanceInput.ProvenanceResourceInput> source = getProvenanceResourceInputs(
                request.queryParamsValues("source"), request.queryParamsValues("source_rel"));
        List<ProvenanceInput.ProvenanceResourceInput> target = getProvenanceResourceInputs(
                request.queryParamsValues("target"), request.queryParamsValues("target_rel"));

        return new ProvenanceInput(
                source,
                target,
                request.queryParams("who"),
                request.queryParams("where"),
                request.queryParams("when"),
                request.queryParams("how"),
                request.queryParams("why"));
    }

    private static List<ProvenanceInput.ProvenanceResourceInput> getProvenanceResourceInputs(
            String[] resources, String[] relations) {
        if (resources == null || relations == null)
            return new ArrayList<>();

        return IntStream.range(0, Math.min(resources.length, relations.length))
                .mapToObj(i -> new ProvenanceInput.ProvenanceResourceInput(resources[i], relations[i]))
                .collect(Collectors.toList());
    }

    private static String validateUris(ProvenanceInput provenanceInput) {
        if (provenanceInput.getWho() != null && !isValidUri(provenanceInput.getWho()))
            return "Invalid URI for 'who'";

        if (provenanceInput.getWhere() != null && !isValidUri(provenanceInput.getWhere()))
            return "Invalid URI for 'where'";

        if (provenanceInput.getWhen() != null && !isValidUri(provenanceInput.getWhen()))
            return "Invalid URI for 'when'";

        if (provenanceInput.getHow() != null && !isValidUri(provenanceInput.getHow()))
            return "Invalid URI for 'how'";

        return null;
    }
}
