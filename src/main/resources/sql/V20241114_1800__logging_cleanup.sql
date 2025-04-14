DROP TABLE geoapi.log.districtresult;
DROP TABLE geoapi.log.geocoderesult;
DROP TABLE geoapi.log.districtrequest;
DROP TABLE geoapi.log.geocoderequest;
DROP TABLE geoapi.log.address;
DROP TABLE geoapi.log.point;
DROP TABLE geoapi.log.exception;

ALTER TABLE geoapi.log.deployment
DROP COLUMN deployed, DROP COLUMN refid,
    ALTER COLUMN deploytime SET DEFAULT NOW();

CREATE TABLE geoapi.log.geocode_stats (
    id SERIAL PRIMARY KEY,
    geocoder TEXT,
    success boolean,
    request_time TIMESTAMP DEFAULT now()
);
