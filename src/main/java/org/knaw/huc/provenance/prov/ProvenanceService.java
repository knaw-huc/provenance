package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static java.sql.Types.CHAR;
import static org.knaw.huc.provenance.util.Config.JDBI;
import static org.knaw.huc.provenance.util.Util.isValidTimestamp;
import static org.knaw.huc.provenance.util.Util.isValidUri;
import static org.knaw.huc.provenance.prov.ProvenanceInput.ProvenanceResourceInput.getInputList;

public class ProvenanceService {
    public Optional<ProvenanceInput> getRecord(int id) {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(ProvenanceSql.SELECT_SQL)
                    .bind("id", id)
                    .map((rs, ctx) -> new ProvenanceInput(
                            getInputList(
                                    Arrays.asList((String[]) rs.getArray("source_res").getArray()),
                                    Arrays.asList((String[]) rs.getArray("source_rel").getArray())
                            ),
                            getInputList(
                                    Arrays.asList((String[]) rs.getArray("target_res").getArray()),
                                    Arrays.asList((String[]) rs.getArray("target_rel").getArray())
                            ),
                            rs.getString("who_person"),
                            rs.getString("where_location"),
                            rs.getString("when_time"),
                            rs.getString("how_software"),
                            rs.getString("why_motivation")
                    ))
                    .findFirst();
        }
    }

    public List<ProvenanceRelation> getTrail(int id) {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(ProvenanceSql.TRAIL_SQL)
                    .bind("id", id)
                    .map((rs, ctx) -> ProvenanceRelation.create(
                            rs.getInt("prov_id"),
                            isValidTimestamp(rs.getString("when_time"))
                                    ? LocalDateTime.ofInstant(
                                            Instant.parse(rs.getString("when_time")), ZoneOffset.UTC)
                                    : null,
                            rs.getString("source_res"), rs.getString("source_rel"),
                            rs.getString("target_res"), rs.getString("target_rel")))
                    .list();
        }
    }

    public int createRecord(ProvenanceInput provenanceInput) {
        try (Handle handle = JDBI.open()) {
            int id = handle.createUpdate(ProvenanceSql.INSERT_SQL)
                    .bind("who", provenanceInput.who())
                    .bind("where", provenanceInput.where())
                    .bind("when", provenanceInput.when())
                    .bind("how_software", provenanceInput.how() != null && isValidUri(provenanceInput.how())
                            ? provenanceInput.how() : new NullArgument(CHAR))
                    .bind("how_delta", provenanceInput.how() != null && !isValidUri(provenanceInput.how())
                            ? provenanceInput.how() : new NullArgument(CHAR))
                    .bind("why_motivation", provenanceInput.why())
                    .bind("why_prov", new NullArgument(CHAR))
                    .executeAndReturnGeneratedKeys()
                    .mapTo(Integer.class)
                    .one();

            insertResources(handle, ProvenanceSql.INSERT_SOURCE_SQL, id, provenanceInput.source());
            insertResources(handle, ProvenanceSql.INSERT_TARGET_SQL, id, provenanceInput.target());

            return id;
        }
    }

    public void updateRecord(int id, ProvenanceInput provenanceInput) {
        try (Handle handle = JDBI.open()) {
            handle.createUpdate(ProvenanceSql.UPDATE_SQL)
                    .bind("who", provenanceInput.who())
                    .bind("where", provenanceInput.where())
                    .bind("when", provenanceInput.when())
                    .bind("how_software", provenanceInput.how() != null && isValidUri(provenanceInput.how())
                            ? provenanceInput.how() : new NullArgument(CHAR))
                    .bind("how_delta", provenanceInput.how() != null && !isValidUri(provenanceInput.how())
                            ? provenanceInput.how() : new NullArgument(CHAR))
                    .bind("why_motivation", provenanceInput.why())
                    .bind("why_prov", new NullArgument(CHAR))
                    .bind("id", id)
                    .execute();

            insertResources(handle, ProvenanceSql.UPSERT_SOURCE_SQL, id, provenanceInput.source());
            insertResources(handle, ProvenanceSql.UPSERT_TARGET_SQL, id, provenanceInput.target());
        }
    }

    private static void insertResources(Handle handle, String sql,
                                        int id, List<ProvenanceInput.ProvenanceResourceInput> resourceInputs) {
        PreparedBatch batch = handle.prepareBatch(sql);
        resourceInputs.forEach(target -> batch
                .bind("prov_id", id)
                .bind("res", target.resource())
                .bind("rel", target.relation())
                .add());
        batch.execute();
    }
}
