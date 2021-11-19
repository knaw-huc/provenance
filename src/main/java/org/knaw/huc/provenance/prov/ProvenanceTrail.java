package org.knaw.huc.provenance.prov;

import java.util.HashSet;
import java.util.Set;

public record ProvenanceTrail(String resource,
                              Set<ProvenanceTrailChild> sources,
                              Set<ProvenanceTrailChild> targets) {
    public ProvenanceTrail(String resource) {
        this(resource, new HashSet<>(), new HashSet<>());
    }

    public ProvenanceTrailChild sourceAsChild() {
        return new ProvenanceTrailChild(0, resource, null, null, sources);
    }

    public ProvenanceTrailChild targetAsChild() {
        return new ProvenanceTrailChild(0, resource, null, null, targets);
    }

    public final record ProvenanceTrailChild(int provenanceId, String resource,
                                             String sourceRelation, String targetRelation,
                                             Set<ProvenanceTrailChild> relations) {
        public ProvenanceTrailChild(int provenanceId, String resource,
                                    String sourceRelation, String targetRelation) {
            this(provenanceId, resource, sourceRelation, targetRelation, new HashSet<>());
        }

        public ProvenanceTrailChild findChild(String resource) {
            if (resource().equals(resource))
                return this;

            for (ProvenanceTrailChild relation : relations()) {
                ProvenanceTrailChild child = relation.findChild(resource);
                if (child != null)
                    return child;
            }

            return null;
        }
    }
}
