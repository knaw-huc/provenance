package org.knaw.huc.provenance.trail;

record ProvenanceTrail<T extends TrailNode<R>, R extends TrailNode<T>>(T sourceRoot, T targetRoot) {
}
