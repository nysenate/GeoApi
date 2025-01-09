ALTER TABLE districts.town
RENAME TO town_city;

UPDATE districts.town_city
SET name = 'Prattsburgh'
WHERE name = 'Prattsburg';
