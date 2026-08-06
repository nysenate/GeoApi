-- Run by update_district_geometry.sh after it reloads districts.village.

-- Corrects to use legal names.
UPDATE districts.village
SET name = replace(name, 'St ', 'St. '),
    county = replace(county, 'St ', 'St. '),
    town = replace(town, 'St ', 'St. ')
WHERE name LIKE '%St %' OR county LIKE '%St %' OR town LIKE '%St %';

-- This village is so new that codes haven't been assigned yet. Let's make that clearer.
UPDATE districts.village
SET gnis_id = '0', fips_code = '0'
WHERE name = 'Ateres' AND gnis_id = '239xxxx' AND fips_code = '361050000000000';
