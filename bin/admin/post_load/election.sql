-- Run by update_district_geometry.sh after it reloads districts.election.

ALTER TABLE districts.election
    ADD COLUMN stored_municipality TEXT,
    ADD COLUMN stored_ed TEXT;

-- Storing these for display later
UPDATE districts.election
SET stored_municipality = municipality,
    stored_ed = election_district;

UPDATE districts.election
SET county = replace(county, 'St ', 'St. '),
    municipality = btrim(replace(municipality, 'St ', 'St. ')),
    election_district = btrim(replace(election_district, 'St ', 'St. '));

UPDATE districts.election
SET municipality = 'Friendship'
WHERE municipality = 'Freindship';

UPDATE districts.election
SET municipality = 'Genesee'
WHERE municipality = 'Gemesee';

UPDATE districts.election
SET municipality = 'Kiantone'
WHERE municipality = 'Kiatone';

-- This village was dissolved in 2010.
UPDATE districts.election
SET municipality = 'Town of Randolph'
WHERE municipality = 'Village of Randolph';

UPDATE districts.election
SET municipality = 'City of Watertown', election_district = replace(election_district, ' (C)', '')
WHERE municipality = 'Watertownc';

UPDATE districts.election
SET election_district = replace(election_district, 'CorningCity', 'Corning City')
WHERE municipality = 'CORNING CITY';

UPDATE districts.election
SET election_district = replace(election_district, 'WestUnion', 'West Union')
WHERE municipality = 'WEST UNION';

--The space is missing in the municipality field, but present in the election_district field.
UPDATE districts.election
SET municipality = 'New Hartford'
WHERE municipality = 'NEWHARTFORD';

UPDATE districts.election
SET election_district = replace(election_district, 'Hardenburg', 'Hardenburgh')
WHERE municipality = 'Hardenburgh';

UPDATE districts.election
SET election_district = replace(election_district, 'Marlboro', 'Marlborough')
WHERE municipality = 'Marlborough';

UPDATE districts.election
SET election_district = replace(election_district, 'Villange', 'Village')
WHERE election_district LIKE 'Villange%';

UPDATE districts.election
SET municipality = 'Village of Portville'
WHERE municipality = 'Town/Village of Portville';

UPDATE districts.election
SET municipality = 'Village of Delevan',
    election_district = replace(election_district, 'Yorkshire ', '')
WHERE municipality = 'Town Yorkshire/Village Delevan';

-- Repairs some bad geometry. 'method=structure' drops collapsed pieces and always returns polygons:
-- the default 'linework' would preserve them as lines, which the geometry column will not take.
UPDATE districts.election
SET geom = ST_Multi(ST_MakeValid(geom, 'method=structure'))
WHERE NOT ST_IsValid(geom);

-- "Lancaster 28" has some problems, including null geometry...
DELETE FROM districts.election
WHERE geom IS NULL;

-- ...and a small ribbon that's clearly a data artifact. This keeps only the bigger portion.
UPDATE districts.election
SET geom = ST_Multi((SELECT d.geom
                     FROM ST_Dump(districts.election.geom) d
                     ORDER BY ST_Area(d.geom) DESC
                     LIMIT 1))
WHERE election_district = 'Lancaster 28';

\ir election_parser.sql
-- Some final municipality cleanup: left until now because it was useful for matching before.
\ir election_muni_cleanup.sql

ALTER TABLE districts.election
    ADD COLUMN town_city_id text,
    ADD COLUMN village_id text;

-- A name is only unique within its county, and a county can hold both a town and a city of it, so
-- a name on its own does not always pick out a single municipality.
UPDATE districts.election e
SET town_city_id = (SELECT string_agg(tc.gnis_id, ',')
                    FROM districts.town_city tc
                    WHERE e.county ILIKE ANY (regexp_split_to_array(tc.county, '\s*,\s*'))
                      -- Uses split_part to remove potential ", County X" suffix.
                      AND replace(split_part(tc.full_name, ',', 1), ' ', '') ILIKE replace(e.tc_name, ' ', ''))
WHERE e.town_city_id IS NULL;

\echo 'EDs not assigned a town_city_id'
SELECT county, stored_municipality, stored_ed, village_name, tc_name
FROM districts.election
WHERE town_city_id IS NULL
ORDER BY county, tc_name, stored_ed;

\echo 'EDs assigned multiple town_city_ids'
SELECT county, stored_municipality, stored_ed, village_name, tc_name
FROM districts.election
WHERE town_city_id LIKE '%,%'
ORDER BY county, tc_name, stored_ed, town_city_id;

-- Village names are unique statewide, but the county is still checked to guard against a bad match.
UPDATE districts.election e
SET village_id = v.gnis_id
FROM districts.village v
WHERE e.village_name IS NOT NULL;

\echo 'EDs with a village name that was not assigned a village_id'
SELECT county, village_name, stored_ed
FROM districts.election
WHERE village_name IS NOT NULL AND village_id IS NULL
ORDER BY county, village_name, stored_ed;

ALTER TABLE districts.election
    ADD COLUMN code text,
    ADD COLUMN name text;

-- Need full town/city names to generate ED names correctly.
UPDATE districts.election e
SET tc_name = tc.full_name
FROM districts.town_city tc
WHERE e.town_city_id = tc.gnis_id;

-- concat_ws drops a null argument, and e,g, ('AD ' || NULL) IS NULL, so a part the ED does not use is skipped by both.
UPDATE districts.election
SET code = concat_ws('-', town_city_id, assembly_district, village_id, ward, county_legislature, display_code),
    name = concat_ws(' ', tc_name || ',',
                     'AD ' || assembly_district,
                     'Village of ' || village_name || ',',
                     'Ward ' || ward,
                     'LD ' || county_legislature,
                     'ED ' || display_code)
WHERE town_city_id IS NOT NULL;

ALTER TABLE districts.election
    DROP COLUMN tc_name,
    DROP COLUMN village_name;

\echo 'Duplicate codes built from different fields (should be empty)'
SELECT code, count(*) AS field_sets
FROM (SELECT DISTINCT code, town_city_id, assembly_district, village_id, ward,
                      county_legislature, display_code
      FROM districts.election
      WHERE code IS NOT NULL) f
GROUP BY code
HAVING count(*) > 1
ORDER BY code;

-- The reverse of the check above: a name is what users see, so it has to separate the districts
-- that the code separates.
\echo 'Names shared by more than one code (should be empty)'
SELECT name, string_agg(DISTINCT code, ', ') AS codes
FROM districts.election
WHERE name IS NOT NULL
GROUP BY name
HAVING count(DISTINCT code) > 1
ORDER BY name;
