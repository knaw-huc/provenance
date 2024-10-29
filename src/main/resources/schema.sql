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

CREATE TABLE source_resources
(
    id        serial PRIMARY KEY,
    prov_id   integer NOT NULL REFERENCES provenance (id),
    res       text,
    rel       text,
    UNIQUE (prov_id, res)
);

CREATE TABLE target_resources
(
    id        serial PRIMARY KEY,
    prov_id   integer NOT NULL REFERENCES provenance (id),
    res       text,
    rel       text,
    UNIQUE (prov_id, res)
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
CREATE INDEX source_resources_prov_id_idx ON source_resources USING hash(prov_id);
CREATE INDEX source_resources_res_idx ON source_resources USING hash(res);
CREATE INDEX target_resources_prov_id_idx ON target_resources USING hash(prov_id);
CREATE INDEX target_resources_res_idx ON target_resources USING hash(res);

CREATE VIEW relations AS
SELECT 's' || id AS id, prov_id, TRUE AS is_source, res, rel
FROM source_resources
UNION ALL
SELECT 't' || id AS id, prov_id, FALSE AS is_source, res, rel
FROM target_resources;
