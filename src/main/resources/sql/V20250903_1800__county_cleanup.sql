ALTER TABLE districts.county
ADD COLUMN senate_code text;

UPDATE districts.county county_geo
SET senate_code = county_public.senate_code
FROM public.county county_public, districts.county
WHERE county_geo.fips_code = county_public.fips_code;
