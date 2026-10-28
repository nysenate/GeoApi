-- Run by update_district_geometry.sh after it reloads districts.town_city.

ALTER TABLE districts.town_city ADD COLUMN code text;

UPDATE districts.town_city SET code = upper(name);

UPDATE districts.town_city
SET code = regexp_replace(code, '^\S+', left(code, 1))
WHERE name ~* '^(North|South|East|West) ';

UPDATE districts.town_city
SET code = regexp_replace(code, '^\S+', left(code, 1) || 'T')
WHERE name ~* '^(Mount|Fort) ';

UPDATE districts.town_city
SET code = replace(code, ' ', '')
WHERE name ~* '^(De|La|Le) ';

UPDATE districts.town_city
SET code = '-' || code
WHERE muni_type = 'city';

ALTER TABLE districts.town_city
    ALTER COLUMN code TYPE varchar(6) USING trim(left(code, 6)),
    ALTER COLUMN code SET NOT NULL;

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
SET code = c.abbrev
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
SET code = c.abbrev
FROM conflicts c
WHERE tc.name = c.name AND tc.county = c.county;

--Corrects to use legal names.
UPDATE districts.town_city
SET name = replace(name, 'St ', 'St. '),
    county = replace(county, 'St ', 'St. ')
WHERE name LIKE '%St %' OR county LIKE '%St %';

-- The codes some counties use for municipalities in their voter files. Only the counties that
-- send us these are listed, so most municipalities have no voterfile_code at all.
ALTER TABLE districts.town_city
    ADD COLUMN IF NOT EXISTS voterfile_code text,
    ADD COLUMN IF NOT EXISTS base_name text;

UPDATE districts.town_city
SET base_name = name;

-- Wyoming County abbreviates by rule rather than by hand: every code is just
-- the first 4 letters of the name.
UPDATE districts.town_city
SET voterfile_code = left(upper(name), 4)
WHERE county = 'Wyoming';

WITH repeated_names AS (
    SELECT name FROM districts.town_city GROUP BY name, muni_type HAVING count(*) > 1
)
UPDATE districts.town_city tc
SET name = CASE WHEN tc.name ILIKE 'New York%' THEN 'New York City'
                     ELSE initcap(tc.muni_type) || ' of ' || tc.name END
                || CASE WHEN tc.name IN (SELECT name FROM repeated_names)
                        THEN ', ' || tc.county || ' County' ELSE '' END;

WITH codes (name, voterfile_code) AS (VALUES
    ('City of Buffalo',           'BFLO'),
    ('City of Lackawanna',        'LACK'),
    ('City of Tonawanda',         'CTON'),
    ('Town of Alden',             'ALDN'),
    ('Town of Amherst',           'AMHS'),
    ('Town of Aurora',            'AURA'),
    ('Town of Boston',            'BOST'),
    ('Town of Brant',             'BRNT'),
    ('Town of Cheektowaga',       'CKTW'),
    ('Town of Clarence',          'CLAR'),
    ('Town of Colden',            'CLDN'),
    ('Town of Collins',           'COLL'),
    ('Town of Concord',           'CONC'),
    ('Town of Evans',             'EVNS'),
    ('Town of Grand Island',      'GRIS'),
    ('Town of Hamburg',           'HAMB'),
    ('Town of Holland',           'HOLL'),
    ('Town of Lancaster',         'LANC'),
    ('Town of Marilla',           'MARL'),
    ('Town of Newstead',          'NEWS'),
    ('Town of North Collins',     'NCOL'),
    ('Town of Orchard Park',      'ORPK'),
    ('Town of Sardinia',          'SARD'),
    ('Town of Tonawanda',         'TTON'),
    ('Town of Wales',             'WALS'),
    ('Town of West Seneca',       'WSEN'),
    ('City of Glen Cove',         'GC'),
    ('City of Long Beach',        'LB'),
    ('Town of Hempstead',         'HEM'),
    ('Town of North Hempstead',   'NH'),
    ('Town of Oyster Bay',        'OB'),
    ('City of Lockport',          'LOCKPORT'),
    ('City of Canandaigua',       'CITY CDGA'),
    ('City of Saratoga Springs',  'SARATOGA SPGS'),
    ('City of Kingston',          'CITY/KNG'),
    ('City of Mount Vernon',      'MTVE'),
    ('City of New Rochelle',      'NEWR'),
    ('City of Peekskill',         'PEEK'),
    ('City of Rye',               'RYE'),
    ('City of White Plains',      'WHPL'),
    ('City of Yonkers',           'YONK'),
    ('Town of Bedford',           'BDFD'),
    ('Town of Cortlandt',         'CORT'),
    ('Town of Eastchester',       'ESTC'),
    ('Town of Greenburgh',        'GRNB'),
    ('Town of Harrison',          'HARR'),
    ('Town of Lewisboro',         'LEWB'),
    ('Town of Mamaroneck',        'MAMA'),
    ('Town of Mount Kisco',       'MTKS'),
    ('Town of Mount Pleasant',    'MTPL'),
    ('Town of New Castle',        'NCTL'),
    ('Town of North Castle',      'NCAS'),
    ('Town of North Salem',       'NSAL'),
    ('Town of Ossining',          'OSSI'),
    ('Town of Pelham',            'PELH'),
    ('Town of Pound Ridge',       'PRDG'),
    ('Town of Rye',               'RYET'),
    ('Town of Scarsdale',         'SCRD'),
    ('Town of Somers',            'SOMR'),
    ('Town of Yorktown',          'YTWN')
)
UPDATE districts.town_city tc
SET voterfile_code = c.voterfile_code
FROM codes c
WHERE tc.name = c.name;
