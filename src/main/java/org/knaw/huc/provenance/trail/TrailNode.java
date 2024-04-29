package org.knaw.huc.provenance.trail;

import java.util.Set;

public interface TrailNode<T> {
    String getType();

    Set<Relation<T>> relations();
}
