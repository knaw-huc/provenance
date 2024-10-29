package org.knaw.huc.provenance.prov;

interface ProvenanceSql {
    String SELECT_BY_PROV_SQL = """
            SELECT id, who_person, where_location, when_time,
                   how_software, how_init, how_delta, why_motivation, why_provenance_schema,
                   source_res, source_rel, target_res, target_rel
            FROM provenance
            LEFT JOIN (
                SELECT array_agg(res) AS source_res, array_agg(rel) AS source_rel
                FROM source_resources
                WHERE prov_id = :id
            ) source ON true
            LEFT JOIN (
                SELECT array_agg(res) AS target_res, array_agg(rel) AS target_rel
                FROM target_resources
                WHERE prov_id = :id
            ) target ON true
            WHERE id = :id""";

    String SELECT_BY_RESOURCE_SQL = """
            SELECT DISTINCT p.id, p.who_person, p.where_location, p.when_time, p.when_timestamp,
                            p.how_software, p.how_init, p.how_delta, p.why_motivation, p.why_provenance_schema,
                            source_res, source_rel, target_res, target_rel
            FROM (
                SELECT DISTINCT provenance.*
                FROM provenance
                INNER JOIN relations
                ON provenance.id = relations.prov_id
                WHERE res = :resource
            ) AS p
            LEFT JOIN LATERAL (
                SELECT array_agg(res) AS source_res, array_agg(rel) AS source_rel
                FROM source_resources
                WHERE prov_id = p.id AND res = :resource
            ) AS source ON TRUE
            LEFT JOIN LATERAL (
                SELECT array_agg(res) AS target_res, array_agg(rel) AS target_rel
                FROM target_resources
                WHERE prov_id = p.id AND res = :resource
            ) AS target ON TRUE
            ORDER BY p.when_timestamp DESC
            LIMIT :limit OFFSET :offset""";

    String SELECT_SOURCE_RESOURCES_SQL = """
            SELECT res, rel
            FROM source_resources
            WHERE prov_id = :id
            LIMIT :limit OFFSET :offset""";

    String SELECT_TARGET_RESOURCES_SQL = """
            SELECT res, rel
            FROM target_resources
            WHERE prov_id = :id
            LIMIT :limit OFFSET :offset""";

    String SELECT_TEMPLATES_SQL = """
            SELECT provenance_column, provenance_value, value_is_regex, description
            FROM provenance_templates""";

    String INSERT_SQL = """
            INSERT INTO provenance (
            who_person, where_location, when_time, how_software, how_init, how_delta,
            why_motivation, why_provenance_schema) VALUES
            (:who, :where, :when, :how_software, :how_init, :how_delta, :why_motivation, :why_prov)""";

    String INSERT_SOURCES_SQL =
            "INSERT INTO source_resources (prov_id, res, rel) VALUES (:prov_id, :res, :rel)";

    String INSERT_TARGETS_SQL =
            "INSERT INTO target_resources (prov_id, res, rel) VALUES (:prov_id, :res, :rel)";

    String UPDATE_SQL = """
            UPDATE provenance SET
            who_person = coalesce(:who, who_person),
            where_location = coalesce(:where, where_location),
            when_time = coalesce(:when, when_time),
            how_software = coalesce(:how_software, how_software),
            how_init = coalesce(:how_init, how_init),
            how_delta = coalesce(:how_delta, how_delta),
            why_motivation = coalesce(:why_motivation, why_motivation),
            why_provenance_schema = coalesce(:why_prov, why_provenance_schema)
            WHERE id = :id""";

    String UPSERT_SOURCES_SQL = """ 
            INSERT INTO source_resources (prov_id, res, rel) VALUES (:prov_id, :res, :rel)
            ON CONFLICT (prov_id, res) DO UPDATE SET rel = :rel""";

    String UPSERT_TARGETS_SQL = """ 
            INSERT INTO target_resources (prov_id, res, rel) VALUES (:prov_id, :res, :rel)
            ON CONFLICT (prov_id, res) DO UPDATE SET rel = :rel""";
}
