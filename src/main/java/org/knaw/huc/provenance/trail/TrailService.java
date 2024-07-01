package org.knaw.huc.provenance.trail;

import org.jdbi.v3.core.Handle;
import org.knaw.huc.provenance.util.Pair;
import org.knaw.huc.provenance.trail.ProvenanceTrailMapper.Direction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.knaw.huc.provenance.trail.ProvenanceTrailMapper.Direction.BACKWARDS;
import static org.knaw.huc.provenance.trail.ProvenanceTrailMapper.Direction.FORWARDS;
import static org.knaw.huc.provenance.util.Config.JDBI;

class TrailService {
    public ProvenanceTrail<Resource, Provenance> getTrailForResource(String resource, LocalDateTime at) {
        try (Handle handle = JDBI.open()) {
            Pair<List<Integer>> startIds = getStartIds(handle, resource, at);

            ProvenanceTrailMapper backwardsMapper = new ProvenanceTrailMapper(resource, BACKWARDS);
            ProvenanceTrailMapper forwardsMapper = new ProvenanceTrailMapper(resource, FORWARDS);
            mapBackwardAndForward(handle, startIds, false, backwardsMapper, forwardsMapper);

            return new ProvenanceTrail<>(backwardsMapper.getResourceRoot(), forwardsMapper.getResourceRoot());
        }
    }

    public Resource getTrailForResource(String resource, LocalDateTime at, Direction direction) {
        try (Handle handle = JDBI.open()) {
            Pair<List<Integer>> startIds = getStartIds(handle, resource, at);
            List<Integer> startIdsForDirection = direction == BACKWARDS ? startIds.first() : startIds.second();

            ProvenanceTrailMapper mapper = new ProvenanceTrailMapper(resource, direction);
            map(handle, startIdsForDirection, mapper, direction);

            return mapper.getResourceRoot();
        }
    }

    public ProvenanceTrail<Provenance, Resource> getTrailForProvenance(long provId) {
        try (Handle handle = JDBI.open()) {
            Pair<List<Integer>> startIds = getStartIds(handle, provId);

            ProvenanceTrailMapper backwardsMapper = new ProvenanceTrailMapper(provId, BACKWARDS);
            ProvenanceTrailMapper forwardsMapper = new ProvenanceTrailMapper(provId, FORWARDS);
            mapBackwardAndForward(handle, startIds, true, backwardsMapper, forwardsMapper);

            return new ProvenanceTrail<>(backwardsMapper.getProvenanceRoot(), forwardsMapper.getProvenanceRoot());
        }
    }

    public Provenance getTrailForProvenance(long provId, Direction direction) {
        try (Handle handle = JDBI.open()) {
            Pair<List<Integer>> startIds = getStartIds(handle, provId);
            List<Integer> startIdsForDirection = direction == BACKWARDS ? startIds.first() : startIds.second();

            ProvenanceTrailMapper mapper = new ProvenanceTrailMapper(provId, direction);
            map(handle, startIdsForDirection, mapper, direction);

            return mapper.getProvenanceRoot();
        }
    }

    private Pair<List<Integer>> getStartIds(Handle handle, String resource, LocalDateTime at) {
        return handle
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
    }

    private Pair<List<Integer>> getStartIds(Handle handle, long provId) {
        return handle
                .createQuery(TrailSql.SELECT_PROVENANCE_RELATIONS_IDS_SQL)
                .bind("provenanceId", provId)
                .reduceResultSet(new Pair<>(new ArrayList<>(), new ArrayList<>()), (prev, rs, ctx) -> {
                    if (!rs.getBoolean("is_source"))
                        prev.first().add(rs.getInt("id"));
                    else if (rs.getBoolean("is_source"))
                        prev.second().add(rs.getInt("id"));
                    return prev;
                });
    }

    private void mapBackwardAndForward(Handle handle, Pair<List<Integer>> startIds, boolean isProvenance,
                                       ProvenanceTrailMapper backwardsMapper, ProvenanceTrailMapper forwardsMapper) {
        if (!startIds.first().isEmpty())
            map(handle, startIds.first(), backwardsMapper, BACKWARDS);
        if (!isProvenance)
            forwardsMapper.setVisited(backwardsMapper.getVisited());
        if (!startIds.second().isEmpty())
            map(handle, startIds.second(), forwardsMapper, FORWARDS);
    }

    private void map(Handle handle, List<Integer> startIds, ProvenanceTrailMapper mapper, Direction direction) {
        String recursiveQuery = direction == FORWARDS ? TrailSql.RECURSIVE_FORWARD_SQL : TrailSql.RECURSIVE_BACKWARD_SQL;
        String query = (recursiveQuery + "\n\n" + TrailSql.RECURSIVE_SQL)
                .replaceAll(":recursive", direction == FORWARDS ? "forward" : "backward");

        handle.createQuery(query)
                .bindList("ids", startIds)
                .map(mapper)
                .list();
    }
}
