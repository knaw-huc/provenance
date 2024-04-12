package org.knaw.huc.provenance.trail;

public interface TrailSql {
    String SELECT_RESOURCE_VERSIONS_RELATIONS_IDS_SQL = """
            -- Using the collected time intervals, find all the resource relations within the given time intervals
            SELECT from_time, to_time, is_source, array_agg(id) AS relation_ids
            FROM prov_relations
            INNER JOIN (
               -- For all timestamps found, create a series of intervals using the lag function
               -- So, 2022-01-01, 2022-01-02, 2022-01-03 becomes
               --
               -- from_time  | to_time
               -- NULL       | 2022-01-01
               -- 2022-01-01 | 2022-01-02
               -- 2022-01-02 | 2022-01-03
               -- 2022-01-03 | NULL        -- For this last NULL we need the UNION with the NULL
               SELECT lag(x, 1) OVER () AS from_time, x AS to_time
               FROM (
                   -- Find all provenance records timestamps of which the given resource is both the source as the target
                   -- Then we know that at this point in time a new version of the resource was created
                   SELECT prov_timestamp
                   FROM prov_relations
                   WHERE res = :resource
                   GROUP BY prov_id, prov_timestamp
                   HAVING array_agg(DISTINCT is_source ORDER BY is_source) = ARRAY [false, true]

                   UNION ALL

                   -- Add an empty record to make the lag function work for our use case
                   SELECT NULL::timestamp

                   ORDER BY prov_timestamp
               ) a(x)
            ) intervals
            ON (from_time IS NULL OR
               (is_source AND prov_timestamp > from_time) OR
               (NOT is_source AND prov_timestamp >= from_time))
            AND (to_time IS NULL OR
                (is_source AND prov_timestamp <= to_time) OR
                (NOT is_source AND prov_timestamp < to_time))
            WHERE res = :resource
            GROUP BY intervals.from_time, intervals.to_time, is_source
            ORDER BY intervals.from_time, intervals.to_time""";

    String SELECT_PROVENANCE_RELATIONS_IDS_SQL = """
            SELECT id, is_source
            FROM relations
            WHERE prov_id = :provenanceId""";

    String TRAIL_BACKWARD_SQL = """
            WITH RECURSIVE backward(id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel) AS (
                SELECT source.id, source.prov_id, source.prov_timestamp, source.res, source.rel, target.res, target.rel
                FROM prov_relations AS target, LATERAL (
                    SELECT *
                    FROM prov_relations
                    WHERE prov_id = target.prov_id AND is_source = true
                ) AS source
                WHERE target.id IN (<ids>) AND target.is_source = false

                UNION ALL

                SELECT source.id, source.prov_id, source.prov_timestamp, source.res, source.rel, target.res, target.rel
                FROM prov_relations AS target, LATERAL (
                    SELECT *
                    FROM prov_relations
                    WHERE prov_id = target.prov_id AND is_source = true
                ) AS source, backward
                WHERE target.res = backward.source_res AND target.is_source = false
            ) CYCLE id SET is_cycle USING path

            SELECT id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel
            FROM (
                SELECT a.*, row_number() OVER () AS sort
                FROM backward AS a
                LEFT JOIN backward AS b
                ON a.id = b.id AND a.path != b.path AND cardinality(a.path) < cardinality(b.path)
                AND NOT (a.path <@ b.path AND a.path @> b.path) AND NOT b.is_cycle
                WHERE b.id IS NULL AND NOT a.is_cycle
            ) AS x
            GROUP BY id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel
            ORDER BY MAX(sort)""";

    String TRAIL_FORWARD_SQL = """
            WITH RECURSIVE forward(id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel) AS (
                SELECT source.id, source.prov_id, source.prov_timestamp, source.res, source.rel, target.res, target.rel
                FROM prov_relations AS source, LATERAL (
                    SELECT *
                    FROM prov_relations
                    WHERE prov_id = source.prov_id AND is_source = false
                ) AS target
                WHERE source.id IN (<ids>) AND source.is_source = true

                UNION ALL

                SELECT source.id, source.prov_id, source.prov_timestamp, source.res, source.rel, target.res, target.rel
                FROM prov_relations AS source, LATERAL (
                    SELECT *
                    FROM prov_relations
                    WHERE prov_id = source.prov_id AND is_source = false
                ) AS target, forward
                WHERE source.res = forward.target_res AND source.is_source = true
            ) CYCLE id SET is_cycle USING path

            SELECT id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel
            FROM (
                SELECT a.*, row_number() OVER () AS sort
                FROM forward AS a
                LEFT JOIN forward AS b
                ON a.id = b.id AND a.path != b.path AND cardinality(a.path) < cardinality(b.path)
                AND NOT (a.path <@ b.path AND a.path @> b.path) AND NOT b.is_cycle
                WHERE b.id IS NULL AND NOT a.is_cycle
            ) AS x
            GROUP BY id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel
            ORDER BY MAX(sort)""";
}
