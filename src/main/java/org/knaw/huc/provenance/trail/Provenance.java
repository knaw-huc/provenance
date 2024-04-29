package org.knaw.huc.provenance.trail;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Provenance(int id, LocalDateTime date, Set<Relation<Resource>> relations) implements TrailNode<Resource> {
    public static Provenance create(int id, LocalDateTime date) {
        return new Provenance(id, date, new HashSet<>());
    }

    @Override
    public String getType() {
        return "provenance";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Provenance other)
            return Objects.equals(id, other.id());
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
