-- Run by update_district_geometry.sh after it reloads districts.electric_utility.

-- comp_id identifies the company, which is enough for the investor-owned utilities, but every
-- municipally-run one is filed under 9999. Those are qualified by the GNIS code of the
-- municipality they serve.
ALTER TABLE districts.electric_utility ADD COLUMN IF NOT EXISTS id text;

UPDATE districts.electric_utility
SET id = comp_id
WHERE name NOT LIKE 'Municipal Utility:%';

-- Most of the municipal utilities are villages.
UPDATE districts.electric_utility eu
SET id = eu.comp_id || '-' || v.gnis_id
FROM districts.village v
WHERE eu.name = 'Municipal Utility: ' || upper(v.name);

-- Jamestown, Plattsburgh, Salamanca and Sherrill run theirs as cities. Only cities are
-- matched: a town of the same name as a village is a different municipality, and the village
-- is the one with the utility. town_city.sql prefixes the names by the time this runs, so the
-- prefix comes back off to compare them.
UPDATE districts.electric_utility eu
SET id = eu.comp_id || '-' || tc.gnis_id
FROM districts.town_city tc
WHERE eu.id IS NULL
  AND tc.muni_type = 'city'
  AND eu.name = 'Municipal Utility: ' || upper(regexp_replace(tc.name, '^City of ', ''));

-- Fishers Island is a hamlet rather than a municipality, so it has no entry of its own in
-- either table. Its utility is keyed on the town it belongs to, Southold.
UPDATE districts.electric_utility eu
SET id = eu.comp_id || '-' || tc.gnis_id
FROM districts.town_city tc
WHERE eu.name = 'Municipal Utility: FISHERS ISLAND'
  AND tc.name= 'Town of Southold';

-- A municipal utility whose municipality wasn't found is left without an id, which the
-- script's SET NOT NULL then fails on. That is deliberate: falling back to a bare 9999 would
-- give two such utilities the same code, and cleanMaps would union them into one district.
\echo 'Utilities with no id (municipality not found in districts.village or districts.town_city):'
SELECT name
FROM districts.electric_utility
WHERE id IS NULL
ORDER BY name;

ALTER TABLE districts.electric_utility
DROP COLUMN comp_id;
