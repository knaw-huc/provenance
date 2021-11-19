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

    String TRAIL_BACKWARD_SQL = """
            WITH RECURSIVE backward(prov_id, source_res, source_rel, target_res, target_rel) AS (
                SELECT source.prov_id, source.res, source.rel, target.res, target.rel
                FROM target, LATERAL (
                    SELECT *
                    FROM source
                    WHERE prov_id = target.prov_id
                ) AS source
                WHERE target.res = :resource

                UNION ALL

                SELECT source.prov_id, source.res, source.rel, target.res, target.rel
                FROM target, LATERAL (
                    SELECT *
                    FROM source
                    WHERE prov_id = target.prov_id
                ) AS source, backward
                WHERE target.res = backward.source_res
            )

            SELECT prov_id, source_res, source_rel, target_res, target_rel FROM backward""";

    String TRAIL_FORWARD_SQL = """
            WITH RECURSIVE forward(prov_id, source_res, source_rel, target_res, target_rel) AS (
                SELECT source.prov_id, source.res, source.rel, target.res, target.rel
                FROM source, LATERAL (
                    SELECT *
                    FROM target
                    WHERE prov_id = source.prov_id
                ) AS target
                WHERE source.res = :resource

                UNION ALL

                SELECT source.prov_id, source.res, source.rel, target.res, target.rel
                FROM source, LATERAL (
                    SELECT *
                    FROM target
                    WHERE prov_id = source.prov_id
                ) AS target, forward
                WHERE source.res = forward.target_res
            )

            SELECT prov_id, source_res, source_rel, target_res, target_rel FROM forward""";

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
