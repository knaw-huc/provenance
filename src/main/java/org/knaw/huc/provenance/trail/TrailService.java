package org.knaw.huc.provenance.trail;

import org.jdbi.v3.core.Handle;
import org.knaw.huc.provenance.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.knaw.huc.provenance.trail.ProvenanceTrailMapper.Direction.BACKWARDS;
import static org.knaw.huc.provenance.trail.ProvenanceTrailMapper.Direction.FORWARDS;
import static org.knaw.huc.provenance.util.Config.JDBI;

public class TrailService {
    public ProvenanceTrail<ProvenanceTrail.Resource, ProvenanceTrail.Provenance> getTrailForResource(
            String resource, LocalDateTime at) {
        try (Handle handle = JDBI.open()) {
            Pair<List<Integer>> startIds = handle
                    .createQuery(TrailSql.SELECT_RESOURCE_VERSIONS_RELATIONS_IDS_SQL)
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
                        if (!rs.getBoolean("is_source") && (prev.first().isEmpty() || at == null))
                            prev.first().addAll(Arrays.asList(ids));
                        else if (rs.getBoolean("is_source") && (prev.second().isEmpty() || at == null))
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
                    .createQuery(TrailSql.SELECT_PROVENANCE_RELATIONS_IDS_SQL)
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
        if (!startIds.first().isEmpty()) {
            handle.createQuery(TrailSql.TRAIL_BACKWARD_SQL)
                    .bindList("ids", startIds.first())
                    .map(backwardsMapper)
                    .list();
        }

        if (!isProvenance)
            forwardsMapper.setVisited(backwardsMapper.getVisited());

        if (!startIds.second().isEmpty()) {
            handle.createQuery(TrailSql.TRAIL_FORWARD_SQL)
                    .bindList("ids", startIds.second())
                    .map(forwardsMapper)
                    .list();
        }
    }
}
