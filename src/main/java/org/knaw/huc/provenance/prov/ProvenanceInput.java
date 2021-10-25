package org.knaw.huc.provenance.prov;

import java.util.List;

public record ProvenanceInput(List<ProvenanceResourceInput> source, List<ProvenanceResourceInput> target,
                              String who, String where, String when, String how, String why) {
    public record ProvenanceResourceInput(String resource, String relation) {
    }
}
