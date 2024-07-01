package org.knaw.huc.provenance.trail;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Resource(String resource, Long provIdUpdate, Set<Relation<Provenance>> relations)
        implements TrailNode<Provenance> {
    public static Resource create(String resource) {
        return new Resource(resource, null, new HashSet<>());
    }

    public Resource createNewVersion(long provIdUpdate) {
        return new Resource(resource, provIdUpdate, new HashSet<>());
    }

    @Override
    public String getType() {
        return "resource";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Resource other)
            return Objects.equals(resource, other.resource()) && Objects.equals(provIdUpdate, other.provIdUpdate());
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, provIdUpdate);
    }
}
