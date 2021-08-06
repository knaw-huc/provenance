package org.knaw.huc.provenance;

import java.util.List;

public class ProvenanceInput {
    private final List<ProvenanceResourceInput> source;
    private final List<ProvenanceResourceInput> target;

    private final String who;
    private final String where;
    private final String when;
    private final String how;
    private final String why;

    public ProvenanceInput(List<ProvenanceResourceInput> source, List<ProvenanceResourceInput> target,
                           String who, String where, String when, String how, String why) {
        this.source = source;
        this.target = target;
        this.who = who;
        this.where = where;
        this.when = when;
        this.how = how;
        this.why = why;
    }

    public List<ProvenanceResourceInput> getSource() {
        return source;
    }

    public List<ProvenanceResourceInput> getTarget() {
        return target;
    }

    public String getWho() {
        return who;
    }

    public String getWhere() {
        return where;
    }

    public String getWhen() {
        return when;
    }

    public String getHow() {
        return how;
    }

    public String getWhy() {
        return why;
    }

    public static final class ProvenanceResourceInput {
        private final String resource;
        private final String relation;

        public ProvenanceResourceInput(String resource, String relation) {
            this.resource = resource;
            this.relation = relation;
        }

        public String getResource() {
            return resource;
        }

        public String getRelation() {
            return relation;
        }
    }
}
