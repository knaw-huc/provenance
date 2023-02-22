package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.knaw.huc.provenance.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;

import static org.knaw.huc.provenance.util.Config.JDBI;
import static org.knaw.huc.provenance.prov.ProvenanceTrailMapper.Direction.*;
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

    public ProvenanceTrail<ProvenanceTrail.Resource, ProvenanceTrail.Provenance> getTrailForResource(
            String resource, LocalDateTime at) {
        try (Handle handle = JDBI.open()) {
            Pair<List<Integer>> startIds = handle
                    .createQuery(ProvenanceSql.SELECT_RESOURCE_VERSIONS_RELATIONS_IDS_SQL)
                    .bind("resource", resource)
                    .reduceResultSet(new Pair<>(new ArrayList<>(), new ArrayList<>()), (prev, rs, ctx) -> {
                        if (at != null) {
                            LocalDateTime fromTime = rs.getObject("from_time", LocalDateTime.class);
                            if (fromTime != null && !fromTime.isBefore(at) && !fromTime.isEqual(at))
                                return prev;

                            LocalDateTime toTime = rs.getObject("to_time", LocalDateTime.class);
                            if (toTime != null && !toTime.isAfter(at) && !toTime.isEqual(at))
                                return prev;
                        }

                        Integer[] ids = (Integer[]) rs.getArray("relation_ids").getArray();
                        if (!rs.getBoolean("is_source") && (prev.first().size() == 0 || at == null))
                            prev.first().addAll(Arrays.asList(ids));
                        else if (rs.getBoolean("is_source") && (prev.second().size() == 0 || at == null))
                            prev.second().addAll(Arrays.asList(ids));

                        return prev;
                    });

            ProvenanceTrailMapper backwardsMapper = new ProvenanceTrailMapper(resource, BACKWARDS);
            ProvenanceTrailMapper forwardsMapper = new ProvenanceTrailMapper(resource, FORWARDS);
            mapBackwardAndForward(handle, startIds, false, backwardsMapper, forwardsMapper);

            return new ProvenanceTrail<>(backwardsMapper.getResourceRoot(), forwardsMapper.getResourceRoot());
        }
    }

    public ProvenanceTrail<ProvenanceTrail.Provenance, ProvenanceTrail.Resource> getTrailForProvenance(int provId) {
        try (Handle handle = JDBI.open()) {
            Pair<List<Integer>> startIds = handle
                    .createQuery(ProvenanceSql.SELECT_PROVENANCE_RELATIONS_IDS_SQL)
                    .bind("provenanceId", provId)
                    .reduceResultSet(new Pair<>(new ArrayList<>(), new ArrayList<>()), (prev, rs, ctx) -> {
                        if (!rs.getBoolean("is_source"))
                            prev.first().add(rs.getInt("id"));
                        else if (rs.getBoolean("is_source"))
                            prev.second().add(rs.getInt("id"));
                        return prev;
                    });

            ProvenanceTrailMapper backwardsMapper = new ProvenanceTrailMapper(provId, BACKWARDS);
            ProvenanceTrailMapper forwardsMapper = new ProvenanceTrailMapper(provId, FORWARDS);
            mapBackwardAndForward(handle, startIds, true, backwardsMapper, forwardsMapper);

            return new ProvenanceTrail<>(backwardsMapper.getProvenanceRoot(), forwardsMapper.getProvenanceRoot());
        }
    }

    private void mapBackwardAndForward(Handle handle, Pair<List<Integer>> startIds, boolean isProvenance,
                                       ProvenanceTrailMapper backwardsMapper, ProvenanceTrailMapper forwardsMapper) {
        if (startIds.first().size() > 0) {
            handle.createQuery(ProvenanceSql.TRAIL_BACKWARD_SQL)
                    .bindList("ids", startIds.first())
                    .map(backwardsMapper)
                    .list();
        }

        if (!isProvenance)
            forwardsMapper.setVisited(backwardsMapper.getVisited());

        if (startIds.second().size() > 0) {
            handle.createQuery(ProvenanceSql.TRAIL_FORWARD_SQL)
                    .bindList("ids", startIds.second())
                    .map(forwardsMapper)
                    .list();
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
