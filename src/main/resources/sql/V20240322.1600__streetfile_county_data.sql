ALTER TABLE geoapi.public.county
    RENAME COLUMN id TO senate_code;
ALTER TABLE geoapi.public.county
    ADD COLUMN streetfile_name VARCHAR;
ALTER TABLE geoapi.public.county
    ADD COLUMN voterfile_code smallint;

UPDATE geoapi.public.county
SET streetfile_name = name;

UPDATE geoapi.public.county
SET streetfile_name = 'Brooklyn'
WHERE name = 'Kings';

UPDATE geoapi.public.county
SET streetfile_name = 'Manhattan'
WHERE name = 'New York';

UPDATE geoapi.public.county
SET streetfile_name = 'Staten Island'
WHERE name = 'Richmond';

--In the voterfile, counties are numbered in alphabetical order...
UPDATE geoapi.public.county
SET voterfile_code = temp.num FROM (
    SELECT name, ROW_NUMBER() OVER(ORDER BY name ASC) AS num
    FROM public.county) AS temp
WHERE temp.name = county.name;

--except for these two, which need to be switched.
UPDATE geoapi.public.county
SET voterfile_code = 50
WHERE name = 'St. Lawrence';

UPDATE geoapi.public.county
SET voterfile_code = 51
WHERE name = 'Steuben';
