package org.knaw.huc.provenance.prov;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.groupingBy;
import static org.knaw.huc.provenance.util.Config.JDBI;

class ProvenanceService {
    public Optional<Provenance> getRecord(int id) {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(ProvenanceSql.SELECT_BY_PROV_SQL)
                    .bind("id", id)
                    .map(Provenance::mapFromResultSet)
                    .findFirst();
        }
    }

    public List<CombinedProvenance> getProvenanceForResource(String resource, int limit, int offset) {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(ProvenanceSql.SELECT_BY_RESOURCE_SQL)
                    .bind("resource", resource)
                    .bind("limit", limit)
                    .bind("offset", offset)
                    .map(Provenance::mapFromResultSet)
                    .stream()
                    .collect(groupingBy(CombinedProvenance::provenanceHash))
                    .values()
                    .stream()
                    .map(CombinedProvenance::mapFromRecords)
                    .collect(toList());
        }
    }

    public List<ProvenanceResource> getResourcesForProvenance(int id, boolean isSource, int limit, int offset) {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(isSource ? ProvenanceSql.SELECT_SOURCE_RESOURCES_SQL : ProvenanceSql.SELECT_TARGET_RESOURCES_SQL)
                    .bind("id", id)
                    .bind("limit", limit)
                    .bind("offset", offset)
                    .map(ProvenanceResource::mapFromResultSet)
                    .collectIntoList();
        }
    }

    public List<ProvenanceTemplate> getProvenanceTemplates() {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(ProvenanceSql.SELECT_TEMPLATES_SQL)
                    .map(ProvenanceTemplate::mapFromResultSet)
                    .collectIntoList();
        }
    }

    public int createRecord(Provenance provenance) {
        try (Handle handle = JDBI.open()) {
            int id = handle.createUpdate(ProvenanceSql.INSERT_SQL)
                    .bind("who", provenance.who())
                    .bind("where", provenance.where())
                    .bind("when", provenance.when())
                    .bind("how_software", provenance.howSoftware())
                    .bind("how_init", provenance.howInit())
                    .bind("how_delta", provenance.howDelta())
                    .bind("why_motivation", provenance.whyMotivation())
                    .bind("why_prov", provenance.whyProvenanceSchema())
                    .executeAndReturnGeneratedKeys()
                    .mapTo(Integer.class)
                    .one();

            insertResources(handle, ProvenanceSql.INSERT_SOURCES_SQL, id, provenance.source());
            insertResources(handle, ProvenanceSql.INSERT_TARGETS_SQL, id, provenance.target());

            return id;
        }
    }

    public void updateRecord(int id, Provenance provenance) {
        try (Handle handle = JDBI.open()) {
            handle.createUpdate(ProvenanceSql.UPDATE_SQL)
                    .bind("who", provenance.who())
                    .bind("where", provenance.where())
                    .bind("when", provenance.when())
                    .bind("how_software", provenance.howSoftware())
                    .bind("how_init", provenance.howInit())
                    .bind("how_delta", provenance.howDelta())
                    .bind("why_motivation", provenance.whyMotivation())
                    .bind("why_prov", provenance.whyProvenanceSchema())
                    .bind("id", id)
                    .execute();

            insertResources(handle, ProvenanceSql.UPSERT_SOURCES_SQL, id, provenance.source());
            insertResources(handle, ProvenanceSql.UPSERT_TARGETS_SQL, id, provenance.target());
        }
    }

    private static void insertResources(Handle handle, String sql, int id, List<ProvenanceResource> resourceInputs) {
        PreparedBatch batch = handle.prepareBatch(sql);
        resourceInputs.forEach(target -> batch
                .bind("prov_id", id)
                .bind("res", target.resource())
                .bind("rel", target.relation())
                .add());
        batch.execute();
    }
}
