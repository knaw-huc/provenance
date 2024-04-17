package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.knaw.huc.provenance.prov.ProvenanceResource.getResourceList;
import static org.knaw.huc.provenance.util.Util.isValidUri;

public record Provenance(long id, String who, String where, String when,
                         String howSoftware, String howInit, String howDelta,
                         String whyMotivation, String whyProvenanceSchema,
                         List<ProvenanceResource> source, List<ProvenanceResource> target) {
    public static Provenance mapFromResultSet(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Provenance(
                rs.getLong("id"),
                rs.getString("who_person"),
                rs.getString("where_location"),
                rs.getString("when_time"),
                rs.getString("how_software"),
                rs.getString("how_init"),
                rs.getString("how_delta"),
                rs.getString("why_motivation"),
                rs.getString("why_provenance_schema"),
                getResourceList(
                        Arrays.asList((String[]) rs.getArray("source_res").getArray()),
                        Arrays.asList((String[]) rs.getArray("source_rel").getArray())
                ),
                getResourceList(
                        Arrays.asList((String[]) rs.getArray("target_res").getArray()),
                        Arrays.asList((String[]) rs.getArray("target_rel").getArray())
                ));
    }

    public static Provenance create(long id, String who, String where, String when, String how, String why,
                                    String howSoftware, String howInit, String howDelta,
                                    String whyMotivation, String whyProvenanceSchema,
                                    List<ProvenanceResource> source, List<ProvenanceResource> target) {
        if (how != null && !how.isEmpty()) {
            if (isValidUri(how)) {
                if (howSoftware == null || howSoftware.isEmpty())
                    howSoftware = how;
            } else if (how.trim().startsWith("+") && how.trim().startsWith("-")) {
                if (howDelta == null || howDelta.isEmpty())
                    howDelta = how;
            } else if (howDelta == null || howDelta.isEmpty())
                howInit = how;
        }

        // TODO: Determine if why is a schema or not
        if (why != null && !why.isEmpty()) {
            if (whyMotivation == null || whyMotivation.isEmpty())
                whyMotivation = why;
        }

        return new Provenance(id, who, where, when,
                howSoftware, howInit, howDelta,
                whyMotivation, whyProvenanceSchema, source, target);
    }
}
