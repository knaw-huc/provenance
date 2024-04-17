package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public record ProvenanceTemplate(String provenance, String value, boolean isRegex, String description) {
    public static ProvenanceTemplate mapFromResultSet(ResultSet rs, StatementContext ctx) throws SQLException {
        return new ProvenanceTemplate(
                rs.getString("provenance_column"),
                rs.getString("provenance_value"),
                rs.getBoolean("value_is_regex"),
                rs.getString("description")
        );
    }
}
