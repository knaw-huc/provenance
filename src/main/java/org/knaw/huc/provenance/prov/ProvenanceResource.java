package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ProvenanceResource(String resource, String relation) {
    public static ProvenanceResource mapFromResultSet(ResultSet rs, StatementContext ctx) throws SQLException {
        return new ProvenanceResource(rs.getString("res"), rs.getString("rel"));
    }

    public static List<ProvenanceResource> getResourceList(List<String> resources, List<String> relations) {
        if (resources == null || relations == null)
            return new ArrayList<>();

        return IntStream.range(0, Math.min(resources.size(), relations.size()))
                .mapToObj(i -> new ProvenanceResource(resources.get(i), relations.get(i)))
                .distinct()
                .collect(Collectors.toList());
    }
}
