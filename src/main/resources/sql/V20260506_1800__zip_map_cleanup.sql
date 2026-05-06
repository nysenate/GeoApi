--Fixes some districts composed of several polygons.
UPDATE districts.zip
SET geom = ST_CollectionExtract(ST_MakeValid(geom, 'method=linework'), 3)
WHERE zip_code IN ('11901', '11967', '13661', '13843');

--Fixes some districts composed of nested polygons.
UPDATE districts.zip
SET geom = ST_CollectionExtract(ST_MakeValid(geom, 'method=structure'), 3)
WHERE zip_code IN ('13145', '13438');
