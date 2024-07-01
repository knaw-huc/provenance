package org.knaw.huc.provenance.trail;

import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public record ProvenanceData(String who, String where, String when,
                             String howSoftware, String howInit, String howDelta,
                             String whyMotivation, String whyProvenanceSchema) {
    public static ProvenanceData mapFromResultSet(ResultSet rs, StatementContext ctx) throws SQLException {
        return new ProvenanceData(
                rs.getString("who_person"),
                rs.getString("where_location"),
                rs.getString("when_time"),
                rs.getString("how_software"),
                rs.getString("how_init"),
                rs.getString("how_delta"),
                rs.getString("why_motivation"),
                rs.getString("why_provenance_schema")
        );
    }
}
