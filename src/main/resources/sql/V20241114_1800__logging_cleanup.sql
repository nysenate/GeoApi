DROP SCHEMA log CASCADE;
CREATE SCHEMA log;

CREATE TABLE log.deployment (
    id INT GENERATED ALWAYS AS IDENTITY,
    deploy_time timestamp DEFAULT NOW() NOT NULL,
    api_requests_since int
);

CREATE TABLE log.geocode_stats (
    id INT GENERATED ALWAYS AS IDENTITY,
    geocoder TEXT,
    success boolean,
    request_time TIMESTAMP DEFAULT now()
);

CREATE TABLE log.api_request (
    id INT GENERATED ALWAYS AS IDENTITY,
    ip_address inet,
    api_user_id INT,
    service TEXT,
    request TEXT,
    params TEXT,
    request_time TIMESTAMP DEFAULT now()
);
