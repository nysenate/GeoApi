DROP TABLE geoapi.public.post_office;

CREATE TABLE geoapi.public.post_office (
    delivery_zip    integer NOT NULL,
    addr1           varchar NOT NULL,
    addr2           varchar,
    city            varchar NOT NULL,
    zip5            integer NOT NULL,
    zip4            integer
);
