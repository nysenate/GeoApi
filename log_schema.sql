--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.9
-- Dumped by pg_dump version 9.1.9
-- Started on 2013-06-05 11:56:17 EDT

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 9 (class 2615 OID 147392)
-- Name: log; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA log;


ALTER SCHEMA log OWNER TO postgres;

SET search_path = log, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 216 (class 1259 OID 147582)
-- Dependencies: 9
-- Name: addresses; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE addresses (
    id integer NOT NULL,
    addr1 character varying(256),
    addr2 character varying(256),
    city character varying(128),
    state character varying(2),
    zip5 character varying(5),
    zip4 character varying(4)
);


ALTER TABLE log.addresses OWNER TO postgres;

--
-- TOC entry 215 (class 1259 OID 147580)
-- Dependencies: 9 216
-- Name: addresses_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE addresses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.addresses_id_seq OWNER TO postgres;

--
-- TOC entry 3116 (class 0 OID 0)
-- Dependencies: 215
-- Name: addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE addresses_id_seq OWNED BY addresses.id;


--
-- TOC entry 212 (class 1259 OID 147545)
-- Dependencies: 9
-- Name: apirequests; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE apirequests (
    id integer NOT NULL,
    ipaddress inet NOT NULL,
    apiuserid integer NOT NULL,
    version integer,
    requesttypeid integer,
    requesttime timestamp without time zone NOT NULL
);


ALTER TABLE log.apirequests OWNER TO postgres;

--
-- TOC entry 211 (class 1259 OID 147543)
-- Dependencies: 9 212
-- Name: apirequests_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE apirequests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.apirequests_id_seq OWNER TO postgres;

--
-- TOC entry 3117 (class 0 OID 0)
-- Dependencies: 211
-- Name: apirequests_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE apirequests_id_seq OWNED BY apirequests.id;


--
-- TOC entry 220 (class 1259 OID 147635)
-- Dependencies: 9
-- Name: districtrequests; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE districtrequests (
    id integer NOT NULL,
    apirequestid integer NOT NULL,
    addressid integer NOT NULL,
    provider character varying(32),
    geoprovider character varying(32),
    showmembers boolean,
    showmaps boolean,
    uspsvalidate boolean,
    skipgeocode boolean,
    districtstrategy character varying(32),
    requesttime timestamp without time zone
);


ALTER TABLE log.districtrequests OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 147633)
-- Dependencies: 220 9
-- Name: districtrequests_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE districtrequests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.districtrequests_id_seq OWNER TO postgres;

--
-- TOC entry 3118 (class 0 OID 0)
-- Dependencies: 219
-- Name: districtrequests_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE districtrequests_id_seq OWNED BY districtrequests.id;


--
-- TOC entry 222 (class 1259 OID 147651)
-- Dependencies: 9
-- Name: districtresults; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE districtresults (
    id integer NOT NULL,
    districtrequestid integer NOT NULL,
    assigned boolean NOT NULL,
    status character varying(32),
    senatecode character varying(3),
    assemblycode character varying(3),
    congressionalcode character varying(3),
    countycode character varying(3),
    resulttime timestamp without time zone NOT NULL
);


ALTER TABLE log.districtresults OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 147649)
-- Dependencies: 222 9
-- Name: districtresults_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE districtresults_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.districtresults_id_seq OWNER TO postgres;

--
-- TOC entry 3119 (class 0 OID 0)
-- Dependencies: 221
-- Name: districtresults_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE districtresults_id_seq OWNED BY districtresults.id;


--
-- TOC entry 214 (class 1259 OID 147574)
-- Dependencies: 9
-- Name: geocoderequests; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE geocoderequests (
    id integer NOT NULL,
    apirequestid integer,
    addressid integer,
    provider character varying(32),
    usefallback boolean,
    usecache boolean,
    requesttime timestamp without time zone
);


ALTER TABLE log.geocoderequests OWNER TO postgres;

--
-- TOC entry 213 (class 1259 OID 147572)
-- Dependencies: 9 214
-- Name: geocoderequests_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE geocoderequests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.geocoderequests_id_seq OWNER TO postgres;

--
-- TOC entry 3120 (class 0 OID 0)
-- Dependencies: 213
-- Name: geocoderequests_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE geocoderequests_id_seq OWNED BY geocoderequests.id;


--
-- TOC entry 218 (class 1259 OID 147604)
-- Dependencies: 1441 9
-- Name: geocoderesults; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE geocoderesults (
    id integer NOT NULL,
    geocoderequestid integer NOT NULL,
    success boolean NOT NULL,
    addressid integer,
    method character varying(32),
    quality character varying(16),
    latlon public.geometry,
    resulttime timestamp without time zone
);


ALTER TABLE log.geocoderesults OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 147602)
-- Dependencies: 218 9
-- Name: geocoderesults_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE geocoderesults_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.geocoderesults_id_seq OWNER TO postgres;

--
-- TOC entry 3121 (class 0 OID 0)
-- Dependencies: 217
-- Name: geocoderesults_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE geocoderesults_id_seq OWNED BY geocoderesults.id;


--
-- TOC entry 210 (class 1259 OID 147508)
-- Dependencies: 9
-- Name: requesttypes; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE requesttypes (
    id integer NOT NULL,
    serviceid integer NOT NULL,
    name character varying(24)
);


ALTER TABLE log.requesttypes OWNER TO postgres;

--
-- TOC entry 209 (class 1259 OID 147506)
-- Dependencies: 210 9
-- Name: requesttypes_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE requesttypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.requesttypes_id_seq OWNER TO postgres;

--
-- TOC entry 3122 (class 0 OID 0)
-- Dependencies: 209
-- Name: requesttypes_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE requesttypes_id_seq OWNED BY requesttypes.id;


--
-- TOC entry 207 (class 1259 OID 147402)
-- Dependencies: 9
-- Name: services; Type: TABLE; Schema: log; Owner: postgres; Tablespace: 
--

CREATE TABLE services (
    id integer NOT NULL,
    name character varying(12)
);


ALTER TABLE log.services OWNER TO postgres;

--
-- TOC entry 208 (class 1259 OID 147408)
-- Dependencies: 9 207
-- Name: services_id_seq; Type: SEQUENCE; Schema: log; Owner: postgres
--

CREATE SEQUENCE services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE log.services_id_seq OWNER TO postgres;

--
-- TOC entry 3123 (class 0 OID 0)
-- Dependencies: 208
-- Name: services_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: postgres
--

ALTER SEQUENCE services_id_seq OWNED BY services.id;


--
-- TOC entry 3083 (class 2604 OID 147585)
-- Dependencies: 216 215 216
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY addresses ALTER COLUMN id SET DEFAULT nextval('addresses_id_seq'::regclass);


--
-- TOC entry 3081 (class 2604 OID 147548)
-- Dependencies: 212 211 212
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY apirequests ALTER COLUMN id SET DEFAULT nextval('apirequests_id_seq'::regclass);


--
-- TOC entry 3085 (class 2604 OID 147638)
-- Dependencies: 219 220 220
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY districtrequests ALTER COLUMN id SET DEFAULT nextval('districtrequests_id_seq'::regclass);


--
-- TOC entry 3086 (class 2604 OID 147654)
-- Dependencies: 221 222 222
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY districtresults ALTER COLUMN id SET DEFAULT nextval('districtresults_id_seq'::regclass);


--
-- TOC entry 3082 (class 2604 OID 147577)
-- Dependencies: 213 214 214
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY geocoderequests ALTER COLUMN id SET DEFAULT nextval('geocoderequests_id_seq'::regclass);


--
-- TOC entry 3084 (class 2604 OID 147607)
-- Dependencies: 218 217 218
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY geocoderesults ALTER COLUMN id SET DEFAULT nextval('geocoderesults_id_seq'::regclass);


--
-- TOC entry 3080 (class 2604 OID 147511)
-- Dependencies: 210 209 210
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY requesttypes ALTER COLUMN id SET DEFAULT nextval('requesttypes_id_seq'::regclass);


--
-- TOC entry 3079 (class 2604 OID 147410)
-- Dependencies: 208 207
-- Name: id; Type: DEFAULT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY services ALTER COLUMN id SET DEFAULT nextval('services_id_seq'::regclass);


--
-- TOC entry 3100 (class 2606 OID 147590)
-- Dependencies: 216 216 3113
-- Name: addresses_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- TOC entry 3095 (class 2606 OID 147553)
-- Dependencies: 212 212 3113
-- Name: apiRequests_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY apirequests
    ADD CONSTRAINT "apiRequests_pkey" PRIMARY KEY (id);


--
-- TOC entry 3104 (class 2606 OID 147640)
-- Dependencies: 220 220 3113
-- Name: districtRequests_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY districtrequests
    ADD CONSTRAINT "districtRequests_pkey" PRIMARY KEY (id);


--
-- TOC entry 3106 (class 2606 OID 147656)
-- Dependencies: 222 222 3113
-- Name: districtResults_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY districtresults
    ADD CONSTRAINT "districtResults_pkey" PRIMARY KEY (id);


--
-- TOC entry 3098 (class 2606 OID 147579)
-- Dependencies: 214 214 3113
-- Name: geocodeRequests_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY geocoderequests
    ADD CONSTRAINT "geocodeRequests_pkey" PRIMARY KEY (id);


--
-- TOC entry 3102 (class 2606 OID 147612)
-- Dependencies: 218 218 3113
-- Name: geocoderesults_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY geocoderesults
    ADD CONSTRAINT geocoderesults_pkey PRIMARY KEY (id);


--
-- TOC entry 3092 (class 2606 OID 147513)
-- Dependencies: 210 210 3113
-- Name: requestTypes_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY requesttypes
    ADD CONSTRAINT "requestTypes_pkey" PRIMARY KEY (id);


--
-- TOC entry 3089 (class 2606 OID 147415)
-- Dependencies: 207 207 3113
-- Name: services_pkey; Type: CONSTRAINT; Schema: log; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY services
    ADD CONSTRAINT services_pkey PRIMARY KEY (id);


--
-- TOC entry 3096 (class 1259 OID 147559)
-- Dependencies: 212 3113
-- Name: fki_requestType; Type: INDEX; Schema: log; Owner: postgres; Tablespace: 
--

CREATE INDEX "fki_requestType" ON apirequests USING btree (requesttypeid);


--
-- TOC entry 3090 (class 1259 OID 147519)
-- Dependencies: 210 3113
-- Name: fki_serviceIdIndex; Type: INDEX; Schema: log; Owner: postgres; Tablespace: 
--

CREATE INDEX "fki_serviceIdIndex" ON requesttypes USING btree (serviceid);


--
-- TOC entry 3093 (class 1259 OID 147520)
-- Dependencies: 210 210 3113
-- Name: requestTypes_serviceId_name_idx; Type: INDEX; Schema: log; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX "requestTypes_serviceId_name_idx" ON requesttypes USING btree (serviceid, name);


--
-- TOC entry 3087 (class 1259 OID 147416)
-- Dependencies: 207 3113
-- Name: services_name_idx; Type: INDEX; Schema: log; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX services_name_idx ON services USING btree (name);


--
-- TOC entry 3111 (class 2606 OID 147657)
-- Dependencies: 3103 222 220 3113
-- Name: districtRequestId_fkey; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY districtresults
    ADD CONSTRAINT "districtRequestId_fkey" FOREIGN KEY (districtrequestid) REFERENCES districtrequests(id);


--
-- TOC entry 3109 (class 2606 OID 147613)
-- Dependencies: 216 3099 218 3113
-- Name: geocoderesults_addressid_fkey; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY geocoderesults
    ADD CONSTRAINT geocoderesults_addressid_fkey FOREIGN KEY (addressid) REFERENCES addresses(id);


--
-- TOC entry 3110 (class 2606 OID 147618)
-- Dependencies: 3097 214 218 3113
-- Name: geocoderesults_geocoderequestid_fkey; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY geocoderesults
    ADD CONSTRAINT geocoderesults_geocoderequestid_fkey FOREIGN KEY (geocoderequestid) REFERENCES geocoderequests(id);


--
-- TOC entry 3108 (class 2606 OID 147554)
-- Dependencies: 212 210 3091 3113
-- Name: requestType; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY apirequests
    ADD CONSTRAINT "requestType" FOREIGN KEY (requesttypeid) REFERENCES requesttypes(id);


--
-- TOC entry 3107 (class 2606 OID 147514)
-- Dependencies: 210 207 3088 3113
-- Name: serviceIdIndex; Type: FK CONSTRAINT; Schema: log; Owner: postgres
--

ALTER TABLE ONLY requesttypes
    ADD CONSTRAINT "serviceIdIndex" FOREIGN KEY (serviceid) REFERENCES services(id);


-- Completed on 2013-06-05 11:56:17 EDT

--
-- PostgreSQL database dump complete
--

