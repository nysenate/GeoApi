DROP TABLE geoapi.public.post_office;

CREATE TABLE geoapi.public.post_office (
    delivery_zip    integer NOT NULL,
    street_with_num varchar NOT NULL,
    city            varchar NOT NULL,
    zip5            integer NOT NULL,
    zip4            integer NOT NULL
);
