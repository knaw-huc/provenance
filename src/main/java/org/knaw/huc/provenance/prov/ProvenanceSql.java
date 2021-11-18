package org.knaw.huc.provenance.prov;

public interface ProvenanceSql {
    String SELECT_SQL = """
            SELECT id, who_person, where_location, when_time,
                   how_software, how_delta, why_motivation, why_provenance_schema,
                   source_res, source_rel, target_res, target_rel
            FROM provenance
            LEFT JOIN (
                SELECT array_agg(res) AS source_res, array_agg(rel) AS source_rel
                FROM source
                WHERE prov_id = :id
            ) source ON true
            LEFT JOIN (
                SELECT array_agg(res) AS target_res, array_agg(rel) AS target_rel
                FROM target
                WHERE prov_id = :id
            ) target ON true
            WHERE id = :id""";

    String TRAIL_SQL = """
            WITH RECURSIVE relations(prov_id, source_res, source_rel, target_res, target_rel, is_cycle, prov_ids) AS (
                SELECT source.prov_id, source.res, source.rel, target.res, target.rel, FALSE, ARRAY[source.prov_id]
                FROM source, LATERAL (
                    SELECT *
                    FROM target
                    WHERE prov_id = source.prov_id
                ) AS target
                WHERE source.prov_id = :id
                        
                UNION ALL
                        
                SELECT * FROM (
                     WITH relations_inner AS (
                         SELECT * FROM relations
                     )
                        
                    SELECT source.prov_id, source.res, source.rel, target.res, target.rel,
                           target.prov_id = ANY(prov_ids), prov_ids || source.prov_id
                    FROM source, LATERAL (
                        SELECT *
                        FROM target
                        WHERE prov_id = source.prov_id
                    ) AS target, relations_inner
                    WHERE source.res = relations_inner.target_res AND NOT is_cycle
                        
                    UNION ALL
                        
                    SELECT source.prov_id, source.res, source.rel, target.res, target.rel,
                           source.prov_id = ANY(prov_ids), prov_ids || target.prov_id
                    FROM target, LATERAL (
                        SELECT *
                        FROM source
                        WHERE prov_id = target.prov_id
                    ) AS source, relations_inner
                    WHERE target.res = relations_inner.source_res AND NOT is_cycle
                ) x
            )
                        
            SELECT DISTINCT prov_id, when_time, source_res, source_rel, target_res, target_rel
            FROM relations
            INNER JOIN provenance
            ON provenance.id = relations.prov_id
            """;

    String INSERT_SQL = """
            INSERT INTO provenance (
            who_person, where_location, when_time, how_software, how_delta,
            why_motivation, why_provenance_schema) VALUES
            (:who, :where, :when, :how_software, :how_delta, :why_motivation, :why_prov)""";

    String INSERT_SOURCE_SQL =
            "INSERT INTO source (prov_id, res, rel) VALUES (:prov_id, :res, :rel)";

    String INSERT_TARGET_SQL =
            "INSERT INTO target (prov_id, res, rel) VALUES (:prov_id, :res, :rel)";

    String UPDATE_SQL = """
            UPDATE provenance SET
            who_person = coalesce(:who, who_person),
            where_location = coalesce(:where, where_location),
            when_time = coalesce(:when, when_time),
            how_software = coalesce(:how_software, how_software),
            how_delta = coalesce(:how_delta, how_delta),
            why_motivation = coalesce(:why_motivation, why_motivation),
            why_provenance_schema = coalesce(:why_prov, why_provenance_schema)
            WHERE id = :id""";

    String UPSERT_SOURCE_SQL = """ 
            INSERT INTO source (prov_id, res, rel) VALUES (:prov_id, :res, :rel)
            ON CONFLICT (prov_id, res) DO UPDATE SET rel = :rel""";

    String UPSERT_TARGET_SQL = """
            INSERT INTO target (prov_id, res, rel) VALUES (:prov_id, :res, :rel)
            ON CONFLICT (prov_id, res) DO UPDATE SET rel = :rel""";
}
