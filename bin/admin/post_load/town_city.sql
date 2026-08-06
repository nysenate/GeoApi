-- Run by update_district_geometry.sh after it reloads districts.town_city.

ALTER TABLE districts.town_city ADD COLUMN display_code text;

UPDATE districts.town_city SET display_code = upper(name);

UPDATE districts.town_city
SET display_code = regexp_replace(display_code, '^\S+', left(display_code, 1))
WHERE name ~* '^(North|South|East|West) ';

UPDATE districts.town_city
SET display_code = regexp_replace(display_code, '^\S+', left(display_code, 1) || 'T')
WHERE name ~* '^(Mount|Fort) ';

UPDATE districts.town_city
SET display_code = replace(display_code, ' ', '')
WHERE name ~* '^(De|La|Le) ';

UPDATE districts.town_city
SET display_code = '-' || display_code
WHERE muni_type = 'city';

ALTER TABLE districts.town_city
    ALTER COLUMN display_code TYPE varchar(6) USING trim(left(display_code, 6)),
    ALTER COLUMN display_code SET NOT NULL;

-- Some codes need to be set manually, mostly because multiple entries would otherwise have the same abbreviation.
WITH conflicts (name, abbrev) AS (VALUES
    ('Alexandria',      'ALEXAD'),
    ('Beekmantown',     'BEEKMT'),
    ('Cambridge',       'CAMBRD'),
    ('Carrollton',      'CARROT'),
    ('Cherry Valley',   'CHERRV'),
    ('Chesterfield',    'CHESTF'),
    ('Clarendon',       'CLARED'),
    ('Clarkstown',      'CLARKT'),
    ('Clarksville',     'CLARKV'),
    ('Clifton Park',    'CLIFTP'),
    ('Columbus',        'COLUMS'),
    ('Constable',       'CONSTB'),
    ('Cortlandville',   'CORTLV'),
    ('Ellicottville',   'ELLICV'),
    ('Forestburgh',     'FORESB'),
    ('Franklinville',   'FRANKV'),
    ('Gainesville',     'GAINEV'),
    ('Genesee Falls',   'GENESF'),
    ('Geneseo',         'GENESO'),
    ('German Flatts',   'GERMAF'),
    ('Germantown',      'GERMAT'),
    ('Greenwood',       'GREEND'),
    ('Hamptonburgh',    'HAMPTB'),
    ('Harrisburg',      'HARRIB'),
    ('Highland',        'HIGHLD'),
    ('Little Valley',   'LITTLV'),
    ('Middleburgh',     'MIDDLB'),
    ('Middlefield',     'MIDDLF'),
    ('Middlesex',       'MIDDLS'),
    ('Morristown',      'MORRIT'),
    ('New Hartford',    'NEW HF'),
    ('New York',        '-NYC'),
    ('Orangetown',      'ORANGT'),
    ('Orangeville',     'ORANGV'),
    ('Parishville',     'PARISV'),
    ('Pittsfield',      'PITTSD'),
    ('Prattsville',     'PRATTV'),
    ('Putnam Valley',   'PUTNAV'),
    ('Red Hook',        'RED HK'),
    ('Richmondville',   'RICHMV'),
    ('Schuyler Falls',  'SCHUYF'),
    ('Seneca Falls',    'SENECF'),
    ('Somerset',        'SOMERT'),
    ('Springfield',     'SPRINF'),
    ('Springport',      'SPRINP'),
    ('Stony Point',     'STONYP'),
    ('Union Vale',      'UNIONV'),
    ('Victory',         'VICTOY'),
    ('Warrensburg',     'WARREB'),
    ('Watervliet',      '-WATEV'),
    ('Western',         'WESTEN'),
    ('Williamstown',    'WILLIM'),
    -- Apparent mistakes rather than conflicts.
    ('Ward',            'W ALMO'),
    ('Wellsville',      'WARD'),
    ('West Almond',     'WELLSV'),
    -- This new town used to be part of Monroe.
    ('Palm Tree',       'MONROE')
)
UPDATE districts.town_city tc
SET display_code = c.abbrev
FROM conflicts c
WHERE tc.name = c.name;

-- The rest share their name with another municipality in a different county.
WITH conflicts (name, county, abbrev) AS (VALUES
    ('Ashland',    'Greene',   'ASHLAD'),
    ('Brighton',   'Franklin', 'BRIGHN'),
    ('Chester',    'Orange',   'CHESTR'),
    ('Clinton',    'Clinton',  'CLINTN'),
    ('Dickinson',  'Franklin', 'DICKIS'),
    ('Franklin',   'Delaware', 'FRANKN'),
    ('Fremont',    'Steuben',  'FREMOT'),
    ('Greenville', 'Orange',   'GREENO'),
    ('Middletown', 'Delaware', 'MIDDLT')
)
UPDATE districts.town_city tc
SET display_code = c.abbrev
FROM conflicts c
WHERE tc.name = c.name AND tc.county = c.county;

--Corrects to use legal names.
UPDATE districts.town_city
SET name = replace(name, 'St ', 'St. '),
    county = replace(county, 'St ', 'St. ')
WHERE name LIKE '%St %' OR county LIKE '%St %';

-- full_name follows the rules TownCity.java builds its fullName by: New York is named for
-- its city rather than its muni_type, and a name more than one municipality of the same
-- type shares is qualified by county.
ALTER TABLE districts.town_city ADD COLUMN IF NOT EXISTS full_name text;

WITH repeated_names AS (
    SELECT name FROM districts.town_city GROUP BY name, muni_type HAVING count(*) > 1
)
UPDATE districts.town_city tc
SET full_name = CASE WHEN tc.name ILIKE 'New York%' THEN 'New York City'
                     ELSE initcap(tc.muni_type) || ' of ' || tc.name END
                || CASE WHEN tc.name IN (SELECT name FROM repeated_names)
                        THEN ', ' || tc.county || ' County' ELSE '' END;
