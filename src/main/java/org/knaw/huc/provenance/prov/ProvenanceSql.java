package org.knaw.huc.provenance.prov;

interface ProvenanceSql {
    String SELECT_BY_PROV_SQL = """
            SELECT id, who_person, where_location, when_time,
                   how_software, how_init, how_delta, why_motivation, why_provenance_schema,
                   source_res, source_rel, target_res, target_rel
            FROM provenance
            LEFT JOIN (
                SELECT array_agg(res) AS source_res, array_agg(rel) AS source_rel
                FROM relations
                WHERE prov_id = :id AND is_source = true
            ) source ON true
            LEFT JOIN (
                SELECT array_agg(res) AS target_res, array_agg(rel) AS target_rel
                FROM relations
                WHERE prov_id = :id AND is_source = false
            ) target ON true
            WHERE id = :id""";

    String SELECT_BY_RESOURCE_SQL = """
            SELECT DISTINCT p.id, p.who_person, p.where_location, p.when_time, p.when_timestamp,
                            p.how_software, p.how_init, p.how_delta, p.why_motivation, p.why_provenance_schema,
                            source_res, source_rel, target_res, target_rel
            FROM relations AS r
            LEFT JOIN provenance AS p ON r.prov_id = p.id
            LEFT JOIN LATERAL (
                SELECT array_agg(res) AS source_res, array_agg(rel) AS source_rel
                FROM relations
                WHERE prov_id = p.id AND is_source = true
            ) AS source ON TRUE
            LEFT JOIN LATERAL (
                SELECT array_agg(res) AS target_res, array_agg(rel) AS target_rel
                FROM relations
                WHERE prov_id = p.id AND is_source = false
            ) AS target ON TRUE
            WHERE r.res = :resource
            ORDER BY p.when_timestamp DESC
            LIMIT :limit OFFSET :offset""";

    String SELECT_RESOURCES_SQL = """
            SELECT res, rel
            FROM relations
            WHERE prov_id = :id AND is_source = :is_source
            LIMIT :limit OFFSET :offset""";

    String SELECT_TEMPLATES_SQL = """
            SELECT provenance_column, provenance_value, value_is_regex, description
            FROM provenance_templates""";

    String INSERT_SQL = """
            INSERT INTO provenance (
            who_person, where_location, when_time, how_software, how_init, how_delta,
            why_motivation, why_provenance_schema) VALUES
            (:who, :where, :when, :how_software, :how_init, :how_delta, :why_motivation, :why_prov)""";

    String INSERT_RELATION_SQL =
            "INSERT INTO relations (prov_id, is_source, res, rel) VALUES (:prov_id, :is_source, :res, :rel)";

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

    String UPSERT_RELATION_SQL = """ 
            INSERT INTO relations (prov_id, is_source, res, rel) VALUES (:prov_id, :is_source, :res, :rel)
            ON CONFLICT (prov_id, is_source, res) DO UPDATE SET rel = :rel""";
}
