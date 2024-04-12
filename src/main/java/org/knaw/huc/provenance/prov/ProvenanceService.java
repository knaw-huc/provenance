package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.knaw.huc.provenance.util.Config.JDBI;
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
                            rs.getString("how_init"),
                            rs.getString("how_delta"),
                            rs.getString("why_motivation"),
                            rs.getString("why_provenance_schema")
                    ))
                    .findFirst();
        }
    }

    public int createRecord(ProvenanceInput provenanceInput) {
        try (Handle handle = JDBI.open()) {
            int id = handle.createUpdate(ProvenanceSql.INSERT_SQL)
                    .bind("who", provenanceInput.who())
                    .bind("where", provenanceInput.where())
                    .bind("when", provenanceInput.when())
                    .bind("how_software", provenanceInput.howSoftware())
                    .bind("how_init", provenanceInput.howInit())
                    .bind("how_delta", provenanceInput.howDelta())
                    .bind("why_motivation", provenanceInput.whyMotivation())
                    .bind("why_prov", provenanceInput.whyProvenanceSchema())
                    .executeAndReturnGeneratedKeys()
                    .mapTo(Integer.class)
                    .one();

            insertResources(handle, ProvenanceSql.INSERT_RELATION_SQL, id, true, provenanceInput.source());
            insertResources(handle, ProvenanceSql.INSERT_RELATION_SQL, id, false, provenanceInput.target());

            return id;
        }
    }

    public void updateRecord(int id, ProvenanceInput provenanceInput) {
        try (Handle handle = JDBI.open()) {
            handle.createUpdate(ProvenanceSql.UPDATE_SQL)
                    .bind("who", provenanceInput.who())
                    .bind("where", provenanceInput.where())
                    .bind("when", provenanceInput.when())
                    .bind("how_software", provenanceInput.howSoftware())
                    .bind("how_init", provenanceInput.howInit())
                    .bind("how_delta", provenanceInput.howDelta())
                    .bind("why_motivation", provenanceInput.whyMotivation())
                    .bind("why_prov", provenanceInput.whyProvenanceSchema())
                    .bind("id", id)
                    .execute();

            insertResources(handle, ProvenanceSql.UPSERT_RELATION_SQL, id, true, provenanceInput.source());
            insertResources(handle, ProvenanceSql.UPSERT_RELATION_SQL, id, false, provenanceInput.target());
        }
    }

    private static void insertResources(Handle handle, String sql, int id, boolean isSource,
                                        List<ProvenanceInput.ProvenanceResourceInput> resourceInputs) {
        PreparedBatch batch = handle.prepareBatch(sql);
        resourceInputs.forEach(target -> batch
                .bind("prov_id", id)
                .bind("is_source", isSource)
                .bind("res", target.resource())
                .bind("rel", target.relation())
                .add());
        batch.execute();
    }
}
