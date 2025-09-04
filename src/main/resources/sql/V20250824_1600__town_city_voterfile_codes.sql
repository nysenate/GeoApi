UPDATE districts.town_city
SET name = 'North East'
WHERE name = 'Northeast';

DROP TABLE IF EXISTS public.town_city;

CREATE TABLE public.town_city (
    voterfile_code text,
    district_code text
);

--For readability and reusability, the "district_code" field is usually first set to the town/city base name.
INSERT INTO public.town_city(voterfile_code, district_code)
VALUES ('HEM', 'Hempstead'), ('NH', 'North Hempstead'),
       ('OB', 'Oyster Bay'), ('SARATOGA SPGS', 'Saratoga Springs'),
       ('MARL', 'Marilla'), ('BFLO', 'Buffalo'),
       ('GRIS', 'Grand Island'), ('CLAR', 'Clarence'),
       ('CLDN', 'Colden'), ('BDFD', 'Bedford'),
       ('NCTL', 'New Castle'), ('GRNB', 'Greenburgh'),
       ('SOMR', 'Somers'), ('CORT', 'Cortlandt'),
       ('NCAS', 'North Castle'), ('NEWR', 'New Rochelle'),
       ('ESTC', 'Eastchester'), ('OSSI', 'Ossining'),
       ('MAMA', 'Mamaroneck'), ('SCRD', 'Scarsdale'),
       ('LANC', 'Lancaster'), ('WSEN', 'West Seneca'),
       ('AMHS', 'Amherst'), ('LACK', 'Lackawanna'),
       ('AURA', 'Aurora'), ('EVNS', 'Evans'),
       ('BOST', 'Boston'), ('HAMB', 'Hamburg'),
       ('ALDN', 'Alden'), ('ORPK', 'Orchard Park'),
       ('CONC', 'Concord'), ('NEWS', 'Newstead'),
       ('COLL', 'Collins'), ('WARS', 'Warsaw'),
       ('GC', 'Glen Cove'), ('LB', 'Long Beach'),
       ('YONK', 'Yonkers'), ('MTPL', 'Mount Pleasant'),
       ('PELH', 'Pelham'), ('MTVE', 'Mount Vernon'),
       ('LEWB', 'Lewisboro'), ('WHPL', 'White Plains'),
       ('YTWN', 'Yorktown'), ('PRDG', 'Pound Ridge'),
       ('MTKS', 'Mount Kisco'), ('PEEK', 'Peekskill'),
       ('HARR', 'Harrison'), ('NCOL', 'North Collins'),
       ('ARCA', 'Arcade'), ('HOLL', 'Holland'),
       ('WALS', 'Wales'), ('PERR', 'Perry'),
       ('ATTI', 'Attica'), ('SARD', 'Sardinia'),
       ('BENN', 'Bennington'), ('BRNT', 'Brant'),
       ('CAST', 'Castile'), ('SHEL', 'Sheldon'),
       ('NSAL', 'North Salem'), ('GAIN', 'Gainesville'),
       ('MIDD', 'Middlebury'), ('ORAN', 'Orangeville'),
       ('EAGL', 'Eagle'), ('COVI', 'Covington'),
       ('WETH', 'Wethersfield'), ('GENE', 'Genesee Falls'),
       ('CKTW', 'Cheektowaga');

UPDATE public.town_city ptc
SET district_code = abbrev
FROM public.town_city, districts.town_city dtc
WHERE ptc.district_code = dtc.name;

--Some codes need to be put in directly, since town_cities may have the same name.
INSERT INTO public.town_city(voterfile_code, district_code)
VALUES ('TTON', 'TONAWA'), ('CTON', '-TONAW'),
       ('RYET', 'RYE'), ('CITY/KNG', '-KINGS'),
       ('CITY CDGA', '-CANAN'),
       --In all cases but these, the name of a town/city by itself would refer to the town.
       ('LOCKPORT', '-LOCKP'), ('RYE', '-RYE');

--Fills out all the town_cities without voter file codes.
INSERT INTO public.town_city(voterfile_code, district_code)
SELECT NULL, abbrev
FROM districts.town_city
WHERE abbrev NOT IN (SELECT town_city.district_code FROM public.town_city);
