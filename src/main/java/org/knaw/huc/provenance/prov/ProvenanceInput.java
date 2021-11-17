package org.knaw.huc.provenance.prov;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record ProvenanceInput(List<ProvenanceResourceInput> source, List<ProvenanceResourceInput> target,
                              String who, String where, String when, String how, String why) {
    public record ProvenanceResourceInput(String resource, String relation) {
        public static List<ProvenanceInput.ProvenanceResourceInput> getInputList(
                List<String> resources, List<String> relations) {
            if (resources == null || relations == null)
                return new ArrayList<>();

            return IntStream.range(0, Math.min(resources.size(), relations.size()))
                    .mapToObj(i -> new ProvenanceInput.ProvenanceResourceInput(resources.get(i), relations.get(i)))
                    .collect(Collectors.toList());
        }
    }
}
