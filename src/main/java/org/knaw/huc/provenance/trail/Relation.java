package org.knaw.huc.provenance.trail;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record Relation<R>(String relation, @JsonUnwrapped R related) {
}
