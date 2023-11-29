package org.knaw.huc.provenance.prov;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.knaw.huc.provenance.util.Util.isValidUri;

public record ProvenanceInput(List<ProvenanceResourceInput> source, List<ProvenanceResourceInput> target,
                              String who, String where, String when,
                              String howSoftware, String howInit, String howDelta,
                              String whyMotivation, String whyProvenanceSchema) {
    public static ProvenanceInput create(List<ProvenanceResourceInput> source, List<ProvenanceResourceInput> target,
                                         String who, String where, String when, String how, String why,
                                         String howSoftware, String howInit, String howDelta,
                                         String whyMotivation, String whyProvenanceSchema) {
        if (how != null && !how.isEmpty()) {
            if (isValidUri(how)) {
                if (howSoftware == null || howSoftware.isEmpty())
                    howSoftware = how;
            } else if (how.trim().startsWith("+") && how.trim().startsWith("-")) {
                if (howDelta == null || howDelta.isEmpty())
                    howDelta = how;
            } else if (howDelta == null || howDelta.isEmpty())
                howInit = how;
        }

        // TODO: Determine if why is a schema or not
        if (why != null && !why.isEmpty()) {
            if (whyMotivation == null || whyMotivation.isEmpty())
                whyMotivation = why;
        }

        return new ProvenanceInput(source, target, who, where, when,
                howSoftware, howInit, howDelta,
                whyMotivation, whyProvenanceSchema);
    }

    public record ProvenanceResourceInput(String resource, String relation) {
        public static List<ProvenanceInput.ProvenanceResourceInput> getInputList(
                List<String> resources, List<String> relations) {
            if (resources == null || relations == null)
                return new ArrayList<>();

            return IntStream.range(0, Math.min(resources.size(), relations.size()))
                    .mapToObj(i -> new ProvenanceInput.ProvenanceResourceInput(resources.get(i), relations.get(i)))
                    .distinct()
                    .collect(Collectors.toList());
        }
    }
}
