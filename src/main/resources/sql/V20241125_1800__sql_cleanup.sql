CREATE FUNCTION isZip(baseZip text, nums int)
    RETURNS BOOLEAN AS $$
BEGIN
    RETURN baseZip != REPEAT('0', nums) AND baseZip SIMILAR TO REPEAT('[0-9]', nums);
END;
$$ LANGUAGE plpgsql;

ALTER TABLE public.geocache
    ADD CONSTRAINT valid_zips CHECK (
        isZip(zip5, 5) AND (zip4 IS NULL OR isZip(zip4, 4))
    );

TRUNCATE TABLE public.streetfile;

ALTER TABLE public.streetfile
    ALTER COLUMN zip5 TYPE varchar(5),
    ADD CONSTRAINT valid_zip CHECK (isZip(zip5, 5)),
    ALTER COLUMN zip5 SET NOT NULL;

TRUNCATE TABLE public.post_office;

ALTER TABLE public.post_office
    ALTER COLUMN delivery_zip TYPE varchar(5),
    ALTER COLUMN zip5 TYPE varchar(5),
    ALTER COLUMN zip4 DROP NOT NULL,
    ALTER COLUMN zip4 TYPE varchar(4),
    ADD CONSTRAINT valid_zips CHECK (
        isZip(delivery_zip, 5) AND
        isZip(zip5, 5) AND
        (zip4 IS NULL OR isZip(zip4, 4))
    );

ALTER TABLE public.post_office
    DROP COLUMN street_with_num;

ALTER TABLE public.post_office
    ADD COLUMN bldg_id text CHECK ( bldg_id IS NOT NULL AND bldg_id != '' );

ALTER TABLE public.post_office
    ADD COLUMN street text CHECK ( street IS NOT NULL AND street != '' );

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
