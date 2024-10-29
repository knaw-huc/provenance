CREATE TYPE provenance_column AS ENUM (
    'who_person', 'where_location', 'when_time', 'how_software', 'how_init',
    'how_delta', 'why_motivation', 'why_provenance_schema'
);

CREATE TABLE provenance
(
    id                    serial PRIMARY KEY,
    who_person            text,
    where_location        text,
    when_time             text,
    when_timestamp        timestamp DEFAULT now() NOT NULL,
    how_software          text,
    how_init              text,
    how_delta             text,
    why_motivation        text,
    why_provenance_schema text
);

CREATE TABLE relations
(
    id        serial PRIMARY KEY,
    prov_id   integer NOT NULL REFERENCES provenance (id),
    is_source boolean NOT NULL,
    res       text,
    rel       text,
    UNIQUE (prov_id, is_source, res)
);

CREATE TABLE provenance_templates
(
    id                serial PRIMARY KEY,
    provenance_column provenance_column NOT NULL,
    provenance_value  text              NOT NULL,
    value_is_regex    boolean           NOT NULL,
    description       text              NOT NULL,
    UNIQUE (provenance_column, provenance_value)
);

CREATE TABLE users
(
    id         uuid PRIMARY KEY,
    email      text,
    who_person text
);

CREATE INDEX when_timestamp_idx ON provenance USING btree(when_timestamp);
CREATE INDEX prov_id_idx ON relations USING hash(prov_id);
CREATE INDEX res_idx ON relations USING hash(res);
CREATE INDEX is_source_res_idx ON relations USING btree(is_source, res);

CREATE VIEW prov_relations AS
SELECT relations.id AS id, prov_id, is_source, res, rel, when_timestamp AS prov_timestamp
FROM relations
INNER JOIN provenance
ON provenance.id = relations.prov_id;
