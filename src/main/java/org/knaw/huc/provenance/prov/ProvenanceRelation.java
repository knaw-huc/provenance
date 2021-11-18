package org.knaw.huc.provenance.prov;

import java.time.LocalDateTime;

public record ProvenanceRelation(int id, LocalDateTime time,
                                 ProvenanceRelationResource source, ProvenanceRelationResource target) {
    public static ProvenanceRelation create(int provId, LocalDateTime time,
                                            String sourceResource, String sourceRelation,
                                            String targetResource, String targetRelation) {
        return new ProvenanceRelation(provId, time,
                new ProvenanceRelationResource(sourceResource, sourceRelation),
                new ProvenanceRelationResource(targetResource, targetRelation));
    }

    private static final record ProvenanceRelationResource(String resource, String relation) {
    }
}
