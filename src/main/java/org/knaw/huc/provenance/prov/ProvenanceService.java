package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.List;

import static java.sql.Types.CHAR;
import static org.knaw.huc.provenance.util.Config.JDBI;

public class ProvenanceService {
    private static final String EXISTS_SQL = "SELECT id FROM provenance WHERE id = :id";

    private static final String INSERT_SQL = """
            INSERT INTO provenance (
            who_person, where_location, when_time, how_software, how_delta,
            why_motivation, why_provenance_schema) VALUES
            (:who, :where, :when, :how_software, :how_delta, :why_motivation, :why_prov)""";

    private static final String INSERT_SOURCE_SQL =
            "INSERT INTO source (prov_id, res, rel) VALUES (:prov_id, :res, :rel)";

    private static final String INSERT_TARGET_SQL =
            "INSERT INTO target (prov_id, res, rel) VALUES (:prov_id, :res, :rel)";

    private static final String UPDATE_SQL = """
            UPDATE provenance SET
            who_person = coalesce(:who, who_person),
            where_location = coalesce(:where, where_location),
            when_time = coalesce(:when, when_time),
            how_software = coalesce(:how_software, how_software),
            how_delta = coalesce(:how_delta, how_delta),
            why_motivation = coalesce(:why_motivation, why_motivation),
            why_provenance_schema = coalesce(:why_prov, why_provenance_schema)
            WHERE id = :id""";

    private static final String UPSERT_SOURCE_SQL = """ 
            INSERT INTO source (prov_id, res, rel) VALUES (:prov_id, :res, :rel)
            ON CONFLICT (prov_id, res) DO UPDATE SET rel = :rel""";

    private static final String UPSERT_TARGET_SQL = """
            INSERT INTO target (prov_id, res, rel) VALUES (:prov_id, :res, :rel)
            ON CONFLICT (prov_id, res) DO UPDATE SET rel = :rel""";

    public boolean recordExists(int id) {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(EXISTS_SQL)
                    .bind("id", id)
                    .mapTo(Integer.class)
                    .findFirst()
                    .isPresent();
        }
    }

    public int createRecord(ProvenanceInput provenanceInput) {
        try (Handle handle = JDBI.open()) {
            int id = handle.createUpdate(INSERT_SQL)
                    .bind("who", provenanceInput.who())
                    .bind("where", provenanceInput.where())
                    .bind("when", provenanceInput.when())
                    .bind("how_software", provenanceInput.how())
                    .bind("how_delta", new NullArgument(CHAR))
                    .bind("why_motivation", provenanceInput.why())
                    .bind("why_prov", new NullArgument(CHAR))
                    .executeAndReturnGeneratedKeys()
                    .mapTo(Integer.class)
                    .one();

            insertResources(handle, INSERT_SOURCE_SQL, id, provenanceInput.source());
            insertResources(handle, INSERT_TARGET_SQL, id, provenanceInput.target());

            return id;
        }
    }

    public void updateRecord(int id, ProvenanceInput provenanceInput) {
        try (Handle handle = JDBI.open()) {
            handle.createUpdate(UPDATE_SQL)
                    .bind("who", provenanceInput.who())
                    .bind("where", provenanceInput.where())
                    .bind("when", provenanceInput.when())
                    .bind("how_software", provenanceInput.how())
                    .bind("how_delta", new NullArgument(CHAR))
                    .bind("why_motivation", provenanceInput.why())
                    .bind("why_prov", new NullArgument(CHAR))
                    .bind("id", id)
                    .execute();

            insertResources(handle, UPSERT_SOURCE_SQL, id, provenanceInput.source());
            insertResources(handle, UPSERT_TARGET_SQL, id, provenanceInput.target());
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
