DROP SCHEMA log;
CREATE SCHEMA log;

CREATE TABLE log.deployment (
    id SERIAL PRIMARY KEY,
    deploy_time timestamp DEFAULT NOW(),
    api_requests_since int
);

CREATE TABLE log.geocode_stats (
    id SERIAL PRIMARY KEY,
    geocoder TEXT,
    success boolean,
    request_time TIMESTAMP DEFAULT now()
);

CREATE TABLE log.api_request (
    id SERIAL PRIMARY KEY,
    ip_address inet,
    api_user_id INT,
    service TEXT,
    request TEXT,
    params TEXT,
    request_time TIMESTAMP DEFAULT now()
);
