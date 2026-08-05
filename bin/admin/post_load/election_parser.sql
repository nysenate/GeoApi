ALTER TABLE districts.election
    ADD COLUMN display_code integer,
    ADD COLUMN county_legislature smallint,
    ADD COLUMN assembly_district smallint,
    ADD COLUMN ward smallint,
    ADD COLUMN muni_type VARCHAR(7),
    ADD COLUMN stored_ed TEXT;

-- TODO: use "name" instead, same at bottom
UPDATE districts.election
SET stored_ed = election_district;

UPDATE districts.election
SET election_district = trim(replace(election_district, county || ' - ', ''));

--Some values start with a 3 letter code.
UPDATE districts.election
SET election_district = regexp_replace(election_district, left(municipality, 3) || '-', '')
WHERE election_district ~ (left(municipality, 3) || '-\d+');

--Helps simplify later regexes.
UPDATE districts.election
SET municipality = translate(municipality, '()', ''),
    election_district = replace(translate(election_district, '()#', ''), ' - ', ' ');

CREATE OR REPLACE FUNCTION muni_type(field text)
RETURNS text
LANGUAGE sql
AS $$
    SELECT initcap(coalesce(
            substring(field FROM '(?i)^(village|town|city) of '), substring(field FROM '(?i) (village|town|city)$')
        ));
$$;

--Lists muni_type conflicts. No conflict if at least one is NULL.
SELECT * FROM districts.election
WHERE coalesce(muni_type(municipality) != muni_type(election_district), false);

UPDATE districts.election
SET muni_type = coalesce(muni_type(municipality), muni_type(election_district))
WHERE coalesce(muni_type(municipality), muni_type(election_district)) IS NOT NULL;

UPDATE districts.election
SET municipality = trim(regexp_replace(municipality, '^(village|town|city) of | (town|city)', '', 'i')),
    election_district = trim(regexp_replace(election_district, '^(village|town|city) of | (town|city)', '', 'i'))
WHERE muni_type IS NOT NULL;

UPDATE districts.election
SET election_district = trim(regexp_replace(election_district, '^' || municipality, '', 'i'));

UPDATE districts.election
SET ward = (regexp_match(election_district, '\y(Ward |WD |W)(\d+)', 'i'))[2]::int,
    election_district = trim(regexp_replace(election_district, '\y(Ward |WD |W)(\d+)', ' ', 'i'))
WHERE election_district ~* '\y(Ward |WD |W)(\d+)';

-- Buffalo's wards are named rather than numbered, and Erie County labels them by the
-- 3-letter abbreviation ('Buffalo ELL 1'). districts.buffalo_ward maps those to the codes
-- SAGE uses (see src/main/resources/sql/add_buffalo_ward_map.sql). An abbreviation the
-- table does not know simply will not match, leaving the label intact and ward NULL.
UPDATE districts.election e
SET ward = w.code,
    election_district = regexp_replace(e.election_district, '^[A-Z]{3} ', '')
FROM districts.buffalo_ward w
WHERE e.municipality = 'Buffalo'
  AND w.abbrev = left(election_district, 3);

UPDATE districts.election
SET assembly_district = (regexp_match(election_district, '\yAD (\d+)'))[1]::int,
    election_district = trim(regexp_replace(election_district, '\yAD (\d+)', ''))
WHERE election_district ~ '\yAD (\d+)';

UPDATE districts.election
SET assembly_district = left(election_district, 2)::smallint,
    election_district = substring(election_district, 3)
WHERE municipality = 'New York';

UPDATE districts.election
SET county_legislature = (regexp_match(election_district, '\y(Leg |LD ?)(\d+)'))[2]::int,
    election_district = trim(regexp_replace(election_district, '\y(Leg |LD ?)(\d+)', ''))
WHERE election_district ~ '\y(Leg |LD ?)(\d+)';

UPDATE districts.election
SET ward = regexp_replace(election_district, '[-/]\d+$', '')::smallint,
    election_district = regexp_replace(election_district, '^\d+[-/]', '')
WHERE election_district ~ '^\d+[-/]\d+$';;

WITH words(word, val) AS (
    VALUES ('ONE',1),('TWO',2),('THREE',3),('FOUR',4), ('FIVE',5), ('SIX', 6)
)
UPDATE districts.election
SET display_code = words.val,
    election_district = ''
FROM words
WHERE election_district = 'DISTRICT ' || words.word;

UPDATE districts.election
SET election_district = trim(
        regexp_replace(election_district, '^(election|(election )?district|e?d) ?(?=\d|$)|(st|nd|rd|th|ed)$', '', 'i')
);

UPDATE districts.election
SET display_code = COALESCE(ward, 1)
WHERE election_district = '';

UPDATE districts.election
SET display_code = election_district::int,
    election_district = ''
WHERE trim(election_district) ~ '^\d+$';

--A few odd, partially-parsed entries remain.
SELECT county, municipality, election_district
FROM districts.election
WHERE election_district != ''
ORDER BY county, municipality, election_district;

UPDATE districts.election
SET display_code = substring(election_district FROM '\d+$')::smallint
WHERE election_district != '';

UPDATE districts.election
SET election_district = stored_ed;
