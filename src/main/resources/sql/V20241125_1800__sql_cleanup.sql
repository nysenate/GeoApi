DROP TABLE IF EXISTS geoapi.public.centroids, geoapi.public.cityzip;
ALTER TABLE log.apirequest DROP COLUMN version;
DROP TRIGGER "updateApiRequestsSinceDeployment" ON log.apirequest;

create function update_requests_since() returns trigger
    language plpgsql
as
$$BEGIN
    WITH latestDeployment AS
             (SELECT d1.id
              FROM log.deployment d1, (SELECT MAX(deploytime) AS latestDeploytime FROM log.deployment) AS m
              WHERE d1.deploytime = latestDeploytime)
    UPDATE log.deployment
    SET apiRequestsSince = apiRequestsSince + 1
    FROM latestDeployment
    WHERE log.deployment.id = latestDeployment.id;
    RETURN new;
END;
$$;

CREATE TRIGGER update_deployment_table AFTER INSERT ON log.apirequest EXECUTE FUNCTION update_requests_since();

DROP TABLE log.apirequest;
CREATE TABLE log.api_request (
    id SERIAL PRIMARY KEY,
    ip_address inet,
    api_user_id INT,
    service TEXT,
    request TEXT,
    params TEXT,
    request_time TIMESTAMP DEFAULT now()
);

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

DROP TABLE log.requesttypes;
DROP TABLE log.services;
