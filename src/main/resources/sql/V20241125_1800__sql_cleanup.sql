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

DROP TABLE districts.election;
CREATE TABLE districts.election (
    -- TODO: should have foreign keys
    id SERIAL PRIMARY KEY,
    election_district SMALLINT,
    town_city_gid SMALLINT,
    assembly_district SMALLINT,
    senate_district SMALLINT,
    county_fips SMALLINT,
    congressional_district SMALLINT,
    geom geometry
);

DROP TABLE log.apirequest;
CREATE TABLE log.api_request (
    id SERIAL PRIMARY KEY,
    ip_address inet,
    api_user_id INT,
    service VARCHAR,
    request VARCHAR,
    request_time TIMESTAMP DEFAULT now()
);
