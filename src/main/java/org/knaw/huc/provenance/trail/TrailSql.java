package org.knaw.huc.provenance.trail;

interface TrailSql {
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

    String RECURSIVE_BACKWARD_SQL = """
            WITH RECURSIVE backward(id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel) AS (
                -- The initial step: start with the given ids and go backwards
                -- res 'is_source' == false --> provenance --> res 'is_source' == true
                -- LATERAL acting as a for-each loop: for each given resource (where 'is_source' == false)
                -- look up the resources from the same provenance record where 'is_source' == true
                SELECT source.id, source.prov_id, source.prov_timestamp, source.res, source.rel, target.res, target.rel
                FROM prov_relations AS target, LATERAL (
                    SELECT *
                    FROM prov_relations
                    WHERE prov_id = target.prov_id AND is_source = true
                ) AS source
                WHERE target.id IN (<ids>) AND target.is_source = false
            
                -- For every recursive step join the results using an union
                UNION ALL

                -- The recursion step: the results so far are in a temporary table 'backward'
                -- Continue going backwards in the same way
                -- Prevent cycles by using the 'id' column to track a 'path', if the 'id' is already in the 'path',
                -- the `is_cycle` column boolean is set to true
                SELECT source.id, source.prov_id, source.prov_timestamp, source.res, source.rel, target.res, target.rel
                FROM prov_relations AS target, LATERAL (
                    SELECT *
                    FROM prov_relations
                    WHERE prov_id = target.prov_id AND is_source = true
                ) AS source, backward
                WHERE target.res = backward.source_res AND target.is_source = false
            ) CYCLE id SET is_cycle USING path""";

    String RECURSIVE_FORWARD_SQL = """
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
            ) CYCLE id SET is_cycle USING path""";

    String RECURSIVE_SQL = """
            -- Join the results from the recursion with the provenance data
            -- Also combine similar provenance records together using a id created by a 'dense_rank' window function
            SELECT trail.id, prov_id, prov_timestamp, who_person, where_location, when_time, when_timestamp,
                   how_software, how_init, how_delta, why_motivation, why_provenance_schema,
                   source_res, source_rel, target_res, target_rel,
                   dense_rank() OVER (ORDER BY who_person, where_location, how_software, how_init, how_delta,
                                               why_motivation, why_provenance_schema) AS combined_prov_id
            FROM (
                -- Filter out duplicates using a group by
                -- Restore the original order using the 'sort' column
                SELECT id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel
                FROM (
                    -- Run the recursion query, but we still have to filter the results
                    -- We use the 'path' to find other paths that lead to the same resource
                    -- Do this by joining the results by checking if the resource is the same, but the paths differ
                    -- We only want to keep those where the path is the longest
                    -- Furthermore, we are not interested in other paths that contain the same path (a 'detour')
                    -- And we have to remove the cycles we detected
                    -- Keep track of a row number 'sort' so we can keep this order later on
                    SELECT a.*, row_number() OVER () AS sort
                    FROM :recursive AS a
                    LEFT JOIN :recursive AS b
                    ON a.id = b.id AND a.path != b.path AND cardinality(a.path) < cardinality(b.path)
                    AND NOT (a.path <@ b.path AND a.path @> b.path) AND NOT b.is_cycle
                    WHERE b.id IS NULL AND NOT a.is_cycle
                ) AS x
                GROUP BY id, prov_id, prov_timestamp, source_res, source_rel, target_res, target_rel
                ORDER BY MAX(sort)
            ) AS trail
            LEFT JOIN provenance AS prov
            ON prov.id = trail.prov_id""";
}
