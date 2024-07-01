package org.knaw.huc.provenance.trail;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public record Provenance(int combinedId, Map<Long, LocalDateTime> records, ProvenanceData data,
                         Set<Relation<Resource>> relations) implements TrailNode<Resource> {
    public static Provenance create(int combinedId, ProvenanceData data) {
        return new Provenance(combinedId, new HashMap<>(), data, new HashSet<>());
    }

    @Override
    public String getType() {
        return "provenance";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Provenance other)
            return Objects.equals(combinedId, other.combinedId());
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(combinedId);
    }
}
