package org.knaw.huc.provenance.prov;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record CombinedProvenance(String who, String where,
                                 String howSoftware, String howInit, String howDelta,
                                 String whyMotivation, String whyProvenanceSchema,
                                 List<ProvenanceRecord> provenance) {
    public static CombinedProvenance mapFromRecords(List<Provenance> provenanceRecords) {
        CombinedProvenance combinedRecords = CombinedProvenance.mapFromRecord(provenanceRecords.removeFirst());
        provenanceRecords.forEach(combinedRecords::addToProvenance);
        return combinedRecords;
    }

    public static CombinedProvenance mapFromRecord(Provenance record) {
        return new CombinedProvenance(
                record.who(), record.where(), record.howSoftware(), record.howInit(),
                record.howDelta(), record.whyMotivation(), record.whyProvenanceSchema(),
                new ArrayList<>(List.of(new ProvenanceRecord(record.id(), record.when(), record.source(), record.target()))));
    }

    public void addToProvenance(Provenance record) {
        provenance.add(new ProvenanceRecord(record.id(), record.when(), record.source(), record.target()));
    }

    public static int provenanceHash(Provenance record) {
        return Objects.hash(record.who(), record.where(), record.howSoftware(),
                record.howInit(), record.howDelta(), record.whyMotivation(), record.whyProvenanceSchema());
    }

    record ProvenanceRecord(long id, String when, List<ProvenanceResource> source, List<ProvenanceResource> target) {
    }
}
