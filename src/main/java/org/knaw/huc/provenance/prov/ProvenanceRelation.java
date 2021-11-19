package org.knaw.huc.provenance.prov;

public record ProvenanceRelation(int id, ProvenanceRelationResource source, ProvenanceRelationResource target) {
    public static ProvenanceRelation create(int provId,
                                            String sourceResource, String sourceRelation,
                                            String targetResource, String targetRelation) {
        return new ProvenanceRelation(provId,
                new ProvenanceRelationResource(sourceResource, sourceRelation),
                new ProvenanceRelationResource(targetResource, targetRelation));
    }

    private static final record ProvenanceRelationResource(String resource, String relation) {
    }
}
