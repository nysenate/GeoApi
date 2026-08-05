-- Run by update_district_geometry.sh after it reloads districts.election.

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
