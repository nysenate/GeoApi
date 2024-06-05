UPDATE districts.town_city
SET name = 'Prattsburgh'
WHERE name = 'Prattsburg';

CREATE TABLE public.streetfile (
    id          SERIAL PRIMARY KEY,
    bldg_low    INT NOT NULL,
    bldg_high   INT NOT NULL,
    parity      parity NOT NULL,
    street      VARCHAR(255) NOT NULL,
    postalCity  VARCHAR(255) NOT NULL,
    zip5        INT NOT NULL,

    congressional_district      SMALLINT REFERENCES congressional (district),
    senate_district             SMALLINT REFERENCES senate (district),
    assembly_district           SMALLINT REFERENCES assembly (district),
    county_fips_code            SMALLINT REFERENCES county (fips_code),
    county_leg_code             SMALLINT,
    ward_code                   SMALLINT,
    election_district           SMALLINT,
    city_council_district       SMALLINT,
    municipal_court_district    SMALLINT,
    town_city_gid               SMALLINT REFERENCES districts.town_city (gid)
)
