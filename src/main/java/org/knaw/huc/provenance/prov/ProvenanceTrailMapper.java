package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ProvenanceTrailMapper implements RowMapper<Void> {
    public enum Direction {BACKWARDS, FORWARDS}

    private final ProvenanceTrail.Resource resourceRoot;
    private ProvenanceTrail.Provenance provenanceRoot;
    private final Direction direction;

    private final Map<Integer, ProvenanceTrail.Provenance> provenances;
    private final Map<String, ProvenanceTrail.Resource> resources;

    public ProvenanceTrailMapper(String resource, Direction dir) {
        resourceRoot = ProvenanceTrail.Resource.create(resource);
        provenanceRoot = null;

        direction = dir;
        provenances = new HashMap<>();
        resources = new HashMap<>();

        resources.put(resource, resourceRoot);
    }

    public ProvenanceTrailMapper(int provenanceId, Direction dir) {
        provenanceRoot = ProvenanceTrail.Provenance.create(provenanceId, null);
        resourceRoot = null;

        direction = dir;
        provenances = new HashMap<>();
        resources = new HashMap<>();
    }

    public ProvenanceTrail.Resource getResourceRoot() {
        return resourceRoot;
    }

    public ProvenanceTrail.Provenance getProvenanceRoot() {
        return provenanceRoot;
    }

    @Override
    public Void map(ResultSet rs, StatementContext ctx) throws SQLException {
        int provId = rs.getInt("prov_id");
        LocalDateTime timestamp = rs.getObject("prov_timestamp", LocalDateTime.class);

        ProvenanceTrail.Provenance provenance = getProvenance(provId, timestamp);
        ProvenanceTrail.Resource source = getResource(rs.getString("source_res"), provId);
        ProvenanceTrail.Resource target = getResource(rs.getString("target_res"), provId);

        if (provenanceRoot != null && provenanceRoot.id() == provId && provenanceRoot.date() == null)
            provenanceRoot = provenance;

        System.out.printf("Mapping %d with %s <-> %s%n", provId, source.resource(), target.resource());

        if (source.equals(target)) {
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

    private ProvenanceTrail.Provenance getProvenance(int provId, LocalDateTime timestamp) {
        ProvenanceTrail.Provenance provenance =
                provenances.getOrDefault(provId, ProvenanceTrail.Provenance.create(provId, timestamp));
        if (!provenances.containsKey(provId))
            provenances.put(provId, provenance);
        return provenance;
    }

    private ProvenanceTrail.Resource getResource(String res, int provId) {
        if (resources.containsKey(res + "_" + provId))
            return resources.get(res + "_" + provId);

        ProvenanceTrail.Resource resource = resources.getOrDefault(res, ProvenanceTrail.Resource.create(res));
        if (!resources.containsKey(res))
            resources.put(res, resource);

        return resource;
    }

    private ProvenanceTrail.Resource createNewResourceVersion(ProvenanceTrail.Resource resource, int provId) {
        ProvenanceTrail.Resource newResource = resource.createNewVersion(provId);
        resources.put(newResource.resource(), newResource);
        resources.put(resource.resource() + "_" + provId, resource);
        return newResource;
    }

    private void addResourceToProvenance(ProvenanceTrail.Provenance provenance,
                                         ProvenanceTrail.Resource source, String sourceRelation,
                                         ProvenanceTrail.Resource target, String targetRelation) {
        source.relations().add(new ProvenanceTrail.Relation<>(sourceRelation, provenance));
        provenance.relations().add(new ProvenanceTrail.Relation<>(targetRelation, target));
    }
}
