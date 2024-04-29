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

            ProvenanceTrailMapper.Direction direction = null;
            try {
                String directionParam = ctx.queryParam("direction");
                if (directionParam != null) {
                    direction = ProvenanceTrailMapper.Direction.valueOf(directionParam.toUpperCase());
                }
            } catch (IllegalArgumentException ignored) {
            }

            if (provenance.hasValue()) {
                if (direction == null) {
                    ProvenanceTrail<Provenance, Resource> provenanceTrail =
                            service.getTrailForProvenance(provenance.get());
                    if (provenanceTrail == null)
                        throw new BadRequestResponse("Invalid provenance id: " + provenance.get());
                    ctx.json(provenanceTrail);
                } else {
                    Provenance provenanceTrail = service.getTrailForProvenance(provenance.get(), direction);
                    if (provenanceTrail == null)
                        throw new BadRequestResponse("Invalid provenance id: " + provenance.get());
                    ctx.json(provenanceTrail);
                }
            } else {
                LocalDateTime at = null;
                String atFormatted = ctx.queryParam("at");
                if (atFormatted != null)
                    at = parseIsoDate(atFormatted);

                if (direction == null) {
                    ProvenanceTrail<Resource, Provenance> provenanceTrail =
                            service.getTrailForResource(resource.trim(), at);
                    if (provenanceTrail == null)
                        throw new BadRequestResponse("Invalid resource: " + resource.trim());
                    ctx.json(provenanceTrail);
                } else {
                    Resource provenanceTrail = service.getTrailForResource(resource.trim(), at, direction);
                    if (provenanceTrail == null)
                        throw new BadRequestResponse("Invalid resource: " + resource.trim());
                    ctx.json(provenanceTrail);
                }
            }
        } catch (DateTimeParseException ex) {
            throw new BadRequestResponse("Incorrect date/time given; use the ISO-8601 format");
        }
    }
}
