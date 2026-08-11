-- Run by update_district_geometry.sh after it reloads districts.county.

ALTER TABLE districts.county ADD COLUMN IF NOT EXISTS senate_code text;

UPDATE districts.county
SET senate_code = (left(swis, 2))::int::text;

-- Corrects to use the legal names.
UPDATE districts.county
SET name = replace(name, 'St ', 'St. ')
WHERE name LIKE '%St %';

-- Nothing should be listed here: every county row in the source carries a SWIS code. If one
-- does appear, the source format has changed and the derivation above needs revisiting.
-- The script's SET NOT NULL on senate_code fails while any row is unmatched.
\echo 'Counties with no senate_code (source SWIS is missing or malformed):'
SELECT name
FROM districts.county
WHERE senate_code IS NULL
ORDER BY name;

\ir add_county_links.sql
