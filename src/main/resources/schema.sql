CREATE TABLE provenance
(
    id                    serial PRIMARY KEY,
    who_person            text,
    where_location        text,
    when_time             text,
    when_timestamp        timestamp DEFAULT now() NOT NULL,
    how_software          text,
    how_delta             text,
    why_motivation        text,
    why_provenance_schema text
);

CREATE TABLE source
(
    prov_id integer NOT NULL REFERENCES provenance (id),
    res     text,
    rel     text,
    PRIMARY KEY (prov_id, res)
);

CREATE TABLE target
(
    prov_id integer NOT NULL REFERENCES provenance (id),
    res     text,
    rel     text,
    PRIMARY KEY (prov_id, res)
);

CREATE TABLE users
(
    id         uuid PRIMARY KEY,
    email      text,
    who_person text
);
