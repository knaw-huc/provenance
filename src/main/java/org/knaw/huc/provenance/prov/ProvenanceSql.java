package org.knaw.huc.provenance.prov;

public interface ProvenanceSql {
    String SELECT_SQL = """
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
