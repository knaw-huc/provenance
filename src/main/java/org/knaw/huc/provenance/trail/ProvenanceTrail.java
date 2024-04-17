package org.knaw.huc.provenance.trail;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

record ProvenanceTrail<T extends ProvenanceTrail.TrailNode<R>, R extends ProvenanceTrail.TrailNode<T>>(
        T sourceRoot, T targetRoot) {
    public record Provenance(int id, LocalDateTime date, Set<Relation<Resource>> relations)
            implements TrailNode<Resource> {
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

    public record Resource(String resource, Integer provIdUpdate, Set<Relation<Provenance>> relations)
            implements TrailNode<Provenance> {
        public static Resource create(String resource) {
            return new Resource(resource, null, new HashSet<>());
        }

        public Resource createNewVersion(int provIdUpdate) {
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

    public record Relation<R>(String relation, @JsonUnwrapped R related) {
    }

    public interface TrailNode<T> {
        String getType();

        Set<Relation<T>> relations();
    }
}

