package org.knaw.huc.provenance.prov;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public record ProvenanceTrail(String resource, Set<ProvenanceTrail> usedBy) {
    public ProvenanceTrail(String resource) {
        this(resource, new HashSet<>());
    }

    public static final class ProvenanceTrailHolder {
        public final Set<String> children = new HashSet<>();
        public final HashMap<String, ProvenanceTrail> index = new HashMap<>();
    }
}
