package org.knaw.huc.provenance.trail;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

class ProvenanceTrailMapper implements RowMapper<Void> {
    public enum Direction {BACKWARDS, FORWARDS}

    private final Resource resourceRoot;
    private Provenance provenanceRoot;
    private final long provenanceRootId;
    private final Direction direction;

    private final Map<Integer, Provenance> provenances;
    private final Map<String, Resource> resources;

    public ProvenanceTrailMapper(String resource, Direction dir) {
        resourceRoot = Resource.create(resource);
        provenanceRoot = null;
        provenanceRootId = 0L;

        direction = dir;
        provenances = new HashMap<>();
        resources = new HashMap<>();

        resources.put(resource, resourceRoot);
    }

    public ProvenanceTrailMapper(long provenanceRootId, Direction dir) {
        this.provenanceRootId = provenanceRootId;
        provenanceRoot = null;
        resourceRoot = null;

        direction = dir;
        provenances = new HashMap<>();
        resources = new HashMap<>();
    }

    public Resource getResourceRoot() {
        return resourceRoot;
    }

    public Provenance getProvenanceRoot() {
        return provenanceRoot;
    }

    @Override
    public Void map(ResultSet rs, StatementContext ctx) throws SQLException {
        int id = rs.getInt("id");

        long provId = rs.getInt("prov_id");
        int combinedProvId = rs.getInt("combined_prov_id");
        LocalDateTime timestamp = rs.getObject("prov_timestamp", LocalDateTime.class);

        ProvenanceData data = ProvenanceData.mapFromResultSet(rs, ctx);
        Provenance provenance = getProvenance(combinedProvId, provId, timestamp, data);
        Resource source = getResource(rs.getString("source_res"), provId);
        Resource target = getResource(rs.getString("target_res"), provId);

        if (provenanceRoot == null && provenanceRootId == provId)
            provenanceRoot = provenance;

        if (source.equals(target)) {
            Resource src = source;
            if (provenance.relations().stream().anyMatch(rel -> rel.related().equals(src)))
                return null;

            if (direction == Direction.FORWARDS)
                target = createNewResourceVersion(target, provId);
            else
                source = createNewResourceVersion(source, provId);
        }

        String sourceRel = rs.getString("source_rel");
        String targetRel = rs.getString("target_rel");

        switch (direction) {
            case FORWARDS -> addResourceToProvenance(provenance, source, sourceRel, target, targetRel);
            case BACKWARDS -> addResourceToProvenance(provenance, target, targetRel, source, sourceRel);
        }

        return null;
    }

    private Provenance getProvenance(int combinedProvId, long provId, LocalDateTime timestamp, ProvenanceData data) {
        Provenance provenance = provenances.getOrDefault(combinedProvId, Provenance.create(combinedProvId, data));
        provenance.records().put(provId, timestamp);

        if (!provenances.containsKey(combinedProvId))
            provenances.put(combinedProvId, provenance);

        return provenance;
    }

    private Resource getResource(String res, long provId) {
        if (resources.containsKey(res + "_" + provId))
            return resources.get(res + "_" + provId);

        Resource resource = resources.getOrDefault(res, Resource.create(res));
        if (!resources.containsKey(res))
            resources.put(res, resource);

        return resource;
    }

    private Resource createNewResourceVersion(Resource resource, long provId) {
        Resource newResource = resource.createNewVersion(provId);
        resources.put(newResource.resource(), newResource);
        resources.put(resource.resource() + "_" + provId, resource);
        return newResource;
    }

    private void addResourceToProvenance(Provenance provenance,
                                         Resource source, String sourceRelation,
                                         Resource target, String targetRelation) {
        source.relations().add(new Relation<>(sourceRelation, provenance));
        provenance.relations().add(new Relation<>(targetRelation, target));
    }
}
