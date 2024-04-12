package org.knaw.huc.provenance.trail;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.validation.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.knaw.huc.provenance.util.Util.parseIsoDate;

public class TrailApi {
    private final TrailService service = new TrailService();

    public void getProvenanceTrail(Context ctx) {
        try {
            Validator<Integer> provenance = ctx.queryParamAsClass("provenance", Integer.class);
            String resource = ctx.queryParam("resource");

            if (!provenance.hasValue() && resource == null)
                throw new BadRequestResponse("No provenance or resource given");

            ProvenanceTrail<?, ?> provenanceTrail;
            if (provenance.hasValue()) {
                provenanceTrail = service.getTrailForProvenance(provenance.get());
                if (provenanceTrail == null)
                    throw new BadRequestResponse("Invalid provenance id: " + provenance.get());
            } else {
                LocalDateTime at = null;
                String atFormatted = ctx.queryParam("at");
                if (atFormatted != null)
                    at = parseIsoDate(atFormatted);

                provenanceTrail = service.getTrailForResource(resource.trim(), at);
                if (provenanceTrail == null)
                    throw new BadRequestResponse("Invalid resource: " + resource.trim());
            }

            ctx.json(provenanceTrail);
        } catch (DateTimeParseException ex) {
            throw new BadRequestResponse("Incorrect date/time given; use the ISO-8601 format");
        }
    }
}
