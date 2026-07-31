-- Run by update_district_geometry.sh after it reloads districts.village.

-- Corrects to use legal names.
UPDATE districts.village
SET name = replace(name, 'St ', 'St. '),
    county = replace(county, 'St ', 'St. '),
    town = replace(town, 'St ', 'St. ')
WHERE name LIKE '%St %' OR county LIKE '%St %' OR town LIKE '%St %';
