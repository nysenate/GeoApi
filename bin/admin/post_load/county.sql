-- Run by update_district_geometry.sh after it reloads districts.county.

ALTER TABLE districts.county ADD COLUMN IF NOT EXISTS code text;

UPDATE districts.county
SET code = (left(swis, 2))::int::text;

-- Corrects to use the legal names.
UPDATE districts.county
SET name = replace(name, 'St ', 'St. ')
WHERE name LIKE '%St %';

-- Nothing should be listed here: every county row in the source carries a SWIS code. If one
-- does appear, the source format has changed and the derivation above needs revisiting.
-- The SET NOT NULL below fails while any row is unmatched.
\echo 'Counties with no code (source SWIS is missing or malformed):'
SELECT name
FROM districts.county
WHERE code IS NULL
ORDER BY name;

ALTER TABLE districts.county ALTER COLUMN code SET NOT NULL;

\ir add_county_links.sql
