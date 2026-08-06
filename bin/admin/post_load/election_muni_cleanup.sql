UPDATE districts.election
SET municipality = regexp_replace(municipality, '^([TCV])[.] ', ''),
    muni_type = CASE left(municipality, 1)
                    WHEN 'T' THEN 'Town'
                    WHEN 'C' THEN 'City'
                    WHEN 'V' THEN 'Village'
                    ELSE muni_type
        END
WHERE municipality ~ '^[TCV][.] ';

-- Forces use of legal name.
UPDATE districts.election
SET municipality = regexp_replace(municipality, '^Saint ', 'St. ', 'i')
WHERE municipality ILIKE 'Saint %';

UPDATE districts.election
SET muni_type = 'Village'
WHERE municipality = 'Scotia';

-- Unfortunately we have to manually set these based on known overlaps.
-- See below for the slightly different districts that will be active in 2027:
-- https://www.chemungcountyny.gov/429/Chemung-County-Polling-Places-Sample-Bal
UPDATE districts.election
SET muni_type = CASE
                    WHEN county_legislature IN (7, 8)
                        OR (county_legislature = 9 AND display_code = 8)
                        THEN 'Town'
                    ELSE 'City'
    END
WHERE municipality = 'Elmira';

ALTER TABLE districts.election
ADD COLUMN village_name text;

UPDATE districts.election
SET village_name = municipality
WHERE muni_type = 'Village';

UPDATE districts.election
-- Cities do not cross with villages.
SET muni_type = 'Town',
    -- In some cases, election_district contains the town name.
    municipality = regexp_replace(election_district, ' \d+$', '')
WHERE muni_type = 'Village' AND election_district ~* '[A-Z]+ \d+$';

-- The parser gets a bit confused by the village named Croghan in the town of the same name.
UPDATE districts.election
SET muni_type = 'Town'
WHERE village_name = 'Croghan';

-- Otherwise, the containing town has to come from the village geometry.
UPDATE districts.election e
SET muni_type = 'Town', municipality = v.town
FROM districts.village v
WHERE lower(e.municipality) = lower(v.name) AND muni_type = 'Village';

-- This field now only has town/city names.
ALTER TABLE districts.election
RENAME municipality TO tc_name;

-- When a town and city have the same name, this is sometimes the only way
-- to distinguish between their respective election districts. Only cities are warded, so a
-- name is a collision when its districts disagree on whether a ward was parsed.
WITH repeat_names AS (
    SELECT tc_name
    FROM districts.election
    GROUP BY tc_name
    HAVING count(DISTINCT (ward IS NULL)) > 1
)
UPDATE districts.election
SET muni_type = CASE
    WHEN ward IS NULL THEN 'Town' ELSE 'City'
END
WHERE muni_type IS NULL AND tc_name IN (SELECT tc_name FROM repeat_names);

-- Anything still untyped takes the type of the tc_name it is named after, but only where
-- the base name is unique.
WITH unique_names AS (
    SELECT county, name, min(muni_type) AS muni_type
    FROM districts.town_city
    GROUP BY county, name
    HAVING count(*) = 1
)
UPDATE districts.election e
SET muni_type = initcap(u.muni_type)
FROM unique_names u
WHERE e.muni_type IS NULL
  AND e.county = u.county
  AND replace(e.tc_name, ' ', '') ILIKE replace(u.name, ' ', '');

-- In these last 2 cases, the city is explicitly named, and the town is not.
UPDATE districts.election
SET muni_type = 'Town'
WHERE muni_type IS NULL AND tc_name IN ('CORNING', 'Batavia');

\echo 'EDs with no parsed municipality type'
SELECT tc_name, election_district
FROM districts.election
WHERE muni_type IS NULL;

UPDATE districts.election
SET tc_name = (initcap(muni_type) || ' of ' || initcap(tc_name))
WHERE muni_type IS NOT NULL AND tc_name != 'New York City';

ALTER TABLE districts.election
DROP COLUMN muni_type;
