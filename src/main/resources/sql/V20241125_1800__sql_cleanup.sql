DROP TABLE IF EXISTS geoapi.public.centroids, geoapi.public.cityzip;

CREATE OR REPLACE FUNCTION area_in_sq_km(geom geometry(MULTIPOLYGON, 4326))
    RETURNS float LANGUAGE plpgsql
AS $$
BEGIN
    RETURN ST_Area(ST_Transform(geom, utmzone(ST_Centroid(geom))))/(1000*1000);
END;$$;

DROP TABLE public.senate CASCADE;

ALTER TABLE public.senator
ADD COLUMN url TEXT;

ALTER TABLE public.assembly
RENAME COLUMN membername TO member_name;
ALTER TABLE public.assembly
RENAME COLUMN memberurl TO member_url;

ALTER TABLE public.congressional
RENAME COLUMN membername TO member_name;
ALTER TABLE public.congressional
RENAME COLUMN memberurl TO member_url;

TRUNCATE TABLE public.post_office;

ALTER TABLE public.post_office
DROP COLUMN street_with_num;

ALTER TABLE public.post_office
    ADD COLUMN bldg_id text CHECK ( bldg_id IS NOT NULL AND bldg_id != '' );

ALTER TABLE public.post_office
    ADD COLUMN street text CHECK ( street IS NOT NULL AND street != '' );

