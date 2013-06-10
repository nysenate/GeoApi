--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: districts; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA districts;


ALTER SCHEMA districts OWNER TO postgres;

--
-- Name: job; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA job;


ALTER SCHEMA job OWNER TO postgres;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


SET search_path = public, pg_catalog;

--
-- Name: parity; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE parity AS ENUM (
    'ODDS',
    'EVENS',
    'ALL'
);


ALTER TYPE public.parity OWNER TO postgres;

SET search_path = districts, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: assembly; Type: TABLE; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE TABLE assembly (
    gid integer NOT NULL,
    id numeric(10,0),
    area double precision,
    district character varying(12),
    members double precision,
    locked character varying(1),
    name character varying(43),
    total_adj double precision,
    population double precision,
    ideal_valu double precision,
    deviation double precision,
    f_deviatio double precision,
    f_populati double precision,
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE districts.assembly OWNER TO postgres;

--
-- Name: assembly_gid_seq; Type: SEQUENCE; Schema: districts; Owner: postgres
--

CREATE SEQUENCE assembly_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.assembly_gid_seq OWNER TO postgres;

--
-- Name: assembly_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: postgres
--

ALTER SEQUENCE assembly_gid_seq OWNED BY assembly.gid;


--
-- Name: congressional; Type: TABLE; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE TABLE congressional (
    gid integer NOT NULL,
    id numeric(10,0),
    area double precision,
    district character varying(12),
    members double precision,
    locked character varying(1),
    name character varying(43),
    population double precision,
    ideal_valu double precision,
    deviation double precision,
    f_deviatio double precision,
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE districts.congressional OWNER TO postgres;

--
-- Name: congressional_gid_seq; Type: SEQUENCE; Schema: districts; Owner: postgres
--

CREATE SEQUENCE congressional_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.congressional_gid_seq OWNER TO postgres;

--
-- Name: congressional_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: postgres
--

ALTER SEQUENCE congressional_gid_seq OWNED BY congressional.gid;


--
-- Name: county; Type: TABLE; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE TABLE county (
    gid integer NOT NULL,
    statefp character varying(2),
    countyfp character varying(3),
    countyns character varying(8),
    cntyidfp character varying(5),
    name character varying(100),
    namelsad character varying(100),
    lsad character varying(2),
    classfp character varying(2),
    mtfcc character varying(5),
    csafp character varying(3),
    cbsafp character varying(5),
    metdivfp character varying(5),
    funcstat character varying(1),
    aland double precision,
    awater double precision,
    intptlat character varying(11),
    intptlon character varying(12),
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE districts.county OWNER TO postgres;

--
-- Name: county_gid_seq; Type: SEQUENCE; Schema: districts; Owner: postgres
--

CREATE SEQUENCE county_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.county_gid_seq OWNER TO postgres;

--
-- Name: county_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: postgres
--

ALTER SEQUENCE county_gid_seq OWNED BY county.gid;


--
-- Name: election; Type: TABLE; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE TABLE election (
    gid integer NOT NULL,
    id double precision,
    area double precision,
    data double precision,
    area1 numeric,
    perimeter numeric,
    eds_copy_ double precision,
    eds_copy_i double precision,
    mcdidchar character varying(12),
    state character varying(2),
    county character varying(3),
    mcd2 smallint,
    ward smallint,
    ed smallint,
    edp double precision,
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE districts.election OWNER TO postgres;

--
-- Name: election_gid_seq; Type: SEQUENCE; Schema: districts; Owner: postgres
--

CREATE SEQUENCE election_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.election_gid_seq OWNER TO postgres;

--
-- Name: election_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: postgres
--

ALTER SEQUENCE election_gid_seq OWNED BY election.gid;


--
-- Name: school; Type: TABLE; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE TABLE school (
    gid integer NOT NULL,
    code numeric,
    name character varying(254),
    polytype character varying(254),
    sqmiles numeric,
    pupilsqmi numeric,
    eacode character varying(254),
    sedcode character varying(254),
    tfcode character varying(254),
    bocescode character varying(254),
    county character varying(254),
    tfname character varying(254),
    sedlegalna character varying(254),
    eaname1 character varying(254),
    eaname2 character varying(254),
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE districts.school OWNER TO postgres;

--
-- Name: school_gid_seq; Type: SEQUENCE; Schema: districts; Owner: postgres
--

CREATE SEQUENCE school_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.school_gid_seq OWNER TO postgres;

--
-- Name: school_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: postgres
--

ALTER SEQUENCE school_gid_seq OWNED BY school.gid;


--
-- Name: senate; Type: TABLE; Schema: districts; Owner: ash; Tablespace: 
--

CREATE TABLE senate (
    gid integer NOT NULL,
    geoid character varying(5),
    namelsad character varying(100),
    lsy character varying(4),
    sd_code character varying(254),
    rep_name character varying(254),
    party character varying(254),
    residence character varying(254),
    since double precision,
    geom public.geometry(MultiPolygon,4269)
);


ALTER TABLE districts.senate OWNER TO ash;

--
-- Name: senate_2013_gid_seq; Type: SEQUENCE; Schema: districts; Owner: ash
--

CREATE SEQUENCE senate_2013_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.senate_2013_gid_seq OWNER TO ash;

--
-- Name: senate_2013_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: ash
--

ALTER SEQUENCE senate_2013_gid_seq OWNED BY senate.gid;


--
-- Name: senate_prev; Type: TABLE; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE TABLE senate_prev (
    gid integer NOT NULL,
    id numeric(10,0),
    area double precision,
    district character varying(12),
    members double precision,
    locked character varying(1),
    name character varying(43),
    total_adj double precision,
    population double precision,
    ideal_valu double precision,
    deviation double precision,
    f_deviatio double precision,
    f_populati double precision,
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE districts.senate_prev OWNER TO postgres;

--
-- Name: senate_gid_seq; Type: SEQUENCE; Schema: districts; Owner: postgres
--

CREATE SEQUENCE senate_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.senate_gid_seq OWNER TO postgres;

--
-- Name: senate_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: postgres
--

ALTER SEQUENCE senate_gid_seq OWNED BY senate_prev.gid;


--
-- Name: town; Type: TABLE; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE TABLE town (
    gid integer NOT NULL,
    name character varying(40),
    ct_type integer,
    gnis_id character varying(9),
    abbrev character varying(6),
    geom public.geometry(MultiPolygon,4326)
);


ALTER TABLE districts.town OWNER TO postgres;

--
-- Name: town_gid_seq; Type: SEQUENCE; Schema: districts; Owner: postgres
--

CREATE SEQUENCE town_gid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE districts.town_gid_seq OWNER TO postgres;

--
-- Name: town_gid_seq; Type: SEQUENCE OWNED BY; Schema: districts; Owner: postgres
--

ALTER SEQUENCE town_gid_seq OWNED BY town.gid;


SET search_path = job, pg_catalog;

--
-- Name: process; Type: TABLE; Schema: job; Owner: postgres; Tablespace: 
--

CREATE TABLE process (
    id integer NOT NULL,
    userid integer NOT NULL,
    filename character varying(256) NOT NULL,
    filetype character varying(128) NOT NULL,
    sourcefilename character varying(256) NOT NULL,
    requesttime timestamp with time zone NOT NULL,
    recordcount integer NOT NULL,
    validationreq boolean,
    geocodereq boolean,
    districtreq boolean
);


ALTER TABLE job.process OWNER TO postgres;

--
-- Name: process_id_seq; Type: SEQUENCE; Schema: job; Owner: postgres
--

CREATE SEQUENCE process_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE job.process_id_seq OWNER TO postgres;

--
-- Name: process_id_seq; Type: SEQUENCE OWNED BY; Schema: job; Owner: postgres
--

ALTER SEQUENCE process_id_seq OWNED BY process.id;


--
-- Name: status; Type: TABLE; Schema: job; Owner: postgres; Tablespace: 
--

CREATE TABLE status (
    processid integer NOT NULL,
    condition character varying(128) NOT NULL,
    completedrecords integer NOT NULL,
    starttime timestamp with time zone,
    completetime timestamp with time zone,
    completed boolean DEFAULT false NOT NULL,
    messages text
);


ALTER TABLE job.status OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: job; Owner: postgres; Tablespace: 
--

CREATE TABLE "user" (
    id integer NOT NULL,
    email character varying(128) NOT NULL,
    password character(60) NOT NULL,
    firstname character varying(128),
    lastname character varying(128),
    active boolean
);


ALTER TABLE job."user" OWNER TO postgres;

--
-- Name: user_id_seq; Type: SEQUENCE; Schema: job; Owner: postgres
--

CREATE SEQUENCE user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE job.user_id_seq OWNER TO postgres;

--
-- Name: user_id_seq; Type: SEQUENCE OWNED BY; Schema: job; Owner: postgres
--

ALTER SEQUENCE user_id_seq OWNED BY "user".id;


SET search_path = public, pg_catalog;

--
-- Name: apiuser; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE apiuser (
    id integer NOT NULL,
    apikey character varying,
    name character varying,
    description text
);


ALTER TABLE public.apiuser OWNER TO postgres;

--
-- Name: apiuser_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE apiuser_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.apiuser_id_seq OWNER TO postgres;

--
-- Name: apiuser_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE apiuser_id_seq OWNED BY apiuser.id;


--
-- Name: assembly; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE assembly (
    district integer NOT NULL,
    membername character varying(128),
    memberurl text
);


ALTER TABLE public.assembly OWNER TO postgres;

--
-- Name: congressional; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE congressional (
    district integer NOT NULL,
    membername character varying(128),
    memberurl text
);


ALTER TABLE public.congressional OWNER TO postgres;

--
-- Name: county; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE county (
    id integer NOT NULL,
    name character varying(32) NOT NULL,
    fips_code integer
);


ALTER TABLE public.county OWNER TO postgres;

--
-- Name: COLUMN county.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN county.id IS 'County code identifier';


--
-- Name: COLUMN county.name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN county.name IS 'County name';


--
-- Name: COLUMN county.fips_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN county.fips_code IS 'County fips code';


--
-- Name: senate; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE senate (
    district integer NOT NULL,
    url character varying(256)
);


ALTER TABLE public.senate OWNER TO postgres;

--
-- Name: senator; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE senator (
    district integer NOT NULL,
    name character varying(128) NOT NULL,
    data text
);


ALTER TABLE public.senator OWNER TO postgres;

--
-- Name: streetfile_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE streetfile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.streetfile_id_seq OWNER TO postgres;
--
--
-- Name: streetfile; Type: TABLE; Schema: public; Owner: ash; Tablespace: 
--

CREATE TABLE streetfile (
    id integer DEFAULT nextval('streetfile_id_seq'::regclass) NOT NULL,
    street character varying(255) COLLATE pg_catalog."en_US.utf8",
    town character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    state character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    zip5 integer,
    bldg_lo_num integer,
    bldg_lo_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    bldg_hi_num integer,
    bldg_hi_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    bldg_parity parity,
    apt_lo_num integer,
    apt_lo_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    apt_hi_num integer,
    apt_hi_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    apt_parity parity,
    election_code integer,
    county_code integer,
    assembly_code integer,
    senate_code integer,
    congressional_code integer,
    boe_town_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    town_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    ward_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    boe_school_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    school_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    cleg_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    cc_code integer,
    fire_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    city_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying,
    vill_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL::character varying
);


ALTER TABLE public.streetfile OWNER TO ash;

--
-- Name: streetfilemap; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE streetfilemap (
    mapcol character varying NOT NULL,
    countycode character varying(255) NOT NULL,
    fromcode character varying(255) NOT NULL,
    tocode character varying(255)
);


ALTER TABLE public.streetfilemap OWNER TO postgres;

SET search_path = districts, pg_catalog;

--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: postgres
--

ALTER TABLE ONLY assembly ALTER COLUMN gid SET DEFAULT nextval('assembly_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: postgres
--

ALTER TABLE ONLY congressional ALTER COLUMN gid SET DEFAULT nextval('congressional_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: postgres
--

ALTER TABLE ONLY county ALTER COLUMN gid SET DEFAULT nextval('county_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: postgres
--

ALTER TABLE ONLY election ALTER COLUMN gid SET DEFAULT nextval('election_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: postgres
--

ALTER TABLE ONLY school ALTER COLUMN gid SET DEFAULT nextval('school_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: ash
--

ALTER TABLE ONLY senate ALTER COLUMN gid SET DEFAULT nextval('senate_2013_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: postgres
--

ALTER TABLE ONLY senate_prev ALTER COLUMN gid SET DEFAULT nextval('senate_gid_seq'::regclass);


--
-- Name: gid; Type: DEFAULT; Schema: districts; Owner: postgres
--

ALTER TABLE ONLY town ALTER COLUMN gid SET DEFAULT nextval('town_gid_seq'::regclass);


SET search_path = job, pg_catalog;

--
-- Name: id; Type: DEFAULT; Schema: job; Owner: postgres
--

ALTER TABLE ONLY process ALTER COLUMN id SET DEFAULT nextval('process_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: job; Owner: postgres
--

ALTER TABLE ONLY "user" ALTER COLUMN id SET DEFAULT nextval('user_id_seq'::regclass);


SET search_path = public, pg_catalog;

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY apiuser ALTER COLUMN id SET DEFAULT nextval('apiuser_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: postgres
--

--
-- Data for Name: spatial_ref_sys; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY spatial_ref_sys (srid, auth_name, auth_srid, srtext, proj4text) FROM stdin;
\.


SET search_path = districts, pg_catalog;

--
-- Name: assembly_pkey; Type: CONSTRAINT; Schema: districts; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY assembly
    ADD CONSTRAINT assembly_pkey PRIMARY KEY (gid);


--
-- Name: congressional_pkey; Type: CONSTRAINT; Schema: districts; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY congressional
    ADD CONSTRAINT congressional_pkey PRIMARY KEY (gid);


--
-- Name: county_pkey; Type: CONSTRAINT; Schema: districts; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY county
    ADD CONSTRAINT county_pkey PRIMARY KEY (gid);


--
-- Name: election_pkey; Type: CONSTRAINT; Schema: districts; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY election
    ADD CONSTRAINT election_pkey PRIMARY KEY (gid);


--
-- Name: school_pkey; Type: CONSTRAINT; Schema: districts; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY school
    ADD CONSTRAINT school_pkey PRIMARY KEY (gid);


--
-- Name: senate_2013_pkey; Type: CONSTRAINT; Schema: districts; Owner: ash; Tablespace: 
--

ALTER TABLE ONLY senate
    ADD CONSTRAINT senate_2013_pkey PRIMARY KEY (gid);


--
-- Name: senate_pkey; Type: CONSTRAINT; Schema: districts; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY senate_prev
    ADD CONSTRAINT senate_pkey PRIMARY KEY (gid);


--
-- Name: town_pkey; Type: CONSTRAINT; Schema: districts; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY town
    ADD CONSTRAINT town_pkey PRIMARY KEY (gid);


SET search_path = job, pg_catalog;

--
-- Name: process_pkey; Type: CONSTRAINT; Schema: job; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY process
    ADD CONSTRAINT process_pkey PRIMARY KEY (id);


--
-- Name: status_pkey; Type: CONSTRAINT; Schema: job; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY status
    ADD CONSTRAINT status_pkey PRIMARY KEY (processid);


--
-- Name: user_email_key; Type: CONSTRAINT; Schema: job; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT user_email_key UNIQUE (email);


--
-- Name: user_pkey; Type: CONSTRAINT; Schema: job; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


SET search_path = public, pg_catalog;

--
-- Name: apiuser_apikey_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY apiuser
    ADD CONSTRAINT apiuser_apikey_key UNIQUE (apikey);


--
-- Name: apiuser_id; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY apiuser
    ADD CONSTRAINT apiuser_id PRIMARY KEY (id);


--
-- Name: assembly_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY assembly
    ADD CONSTRAINT assembly_pkey PRIMARY KEY (district);


--
-- Name: congressional_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY congressional
    ADD CONSTRAINT congressional_pkey PRIMARY KEY (district);


--
-- Name: county_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY county
    ADD CONSTRAINT county_pkey PRIMARY KEY (id);


--
-- Name: senate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY senate
    ADD CONSTRAINT senate_pkey PRIMARY KEY (district);


--
-- Name: senator_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY senator
    ADD CONSTRAINT senator_name_key UNIQUE (name);


--
-- Name: senator_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY senator
    ADD CONSTRAINT senator_pkey PRIMARY KEY (district);


--
-- Name: streetfilemap_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY streetfilemap
    ADD CONSTRAINT streetfilemap_pkey PRIMARY KEY (mapcol, countycode, fromcode);


SET search_path = districts, pg_catalog;

--
-- Name: assembly_geom_gist; Type: INDEX; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE INDEX assembly_geom_gist ON assembly USING gist (geom);


--
-- Name: congressional_geom_gist; Type: INDEX; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE INDEX congressional_geom_gist ON congressional USING gist (geom);


--
-- Name: county_geom_gist; Type: INDEX; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE INDEX county_geom_gist ON county USING gist (geom);


--
-- Name: election_geom_gist; Type: INDEX; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE INDEX election_geom_gist ON election USING gist (geom);


--
-- Name: school_geom_gist; Type: INDEX; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE INDEX school_geom_gist ON school USING gist (geom);


--
-- Name: senate_2013_geom_gist; Type: INDEX; Schema: districts; Owner: ash; Tablespace: 
--

CREATE INDEX senate_2013_geom_gist ON senate USING gist (geom);


--
-- Name: senate_geom_gist; Type: INDEX; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE INDEX senate_geom_gist ON senate_prev USING gist (geom);


--
-- Name: town_geom_gist; Type: INDEX; Schema: districts; Owner: postgres; Tablespace: 
--

CREATE INDEX town_geom_gist ON town USING gist (geom);


SET search_path = public, pg_catalog;

--
-- Name: county_fips_code_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX county_fips_code_idx ON county USING btree (fips_code);


--
-- Name: county_name_idx; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE UNIQUE INDEX county_name_idx ON county USING btree (name);


--
-- Name: fki_district_fk; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX fki_district_fk ON senator USING btree (district);


--
-- Name: streetfile_street_zip5_bldg_lo_num_bldg_hi_num_bldg_lo_chr_idx; Type: INDEX; Schema: public; Owner: ash; Tablespace: 
--

CREATE INDEX streetfile_street_zip5_bldg_lo_num_bldg_hi_num_bldg_lo_chr_idx ON streetfile USING btree (street, zip5, bldg_lo_num, bldg_hi_num, bldg_lo_chr);


--
-- Name: geometry_columns_delete; Type: RULE; Schema: public; Owner: postgres
--

CREATE RULE geometry_columns_delete AS ON DELETE TO geometry_columns DO INSTEAD NOTHING;


--
-- Name: geometry_columns_insert; Type: RULE; Schema: public; Owner: postgres
--

CREATE RULE geometry_columns_insert AS ON INSERT TO geometry_columns DO INSTEAD NOTHING;


--
-- Name: geometry_columns_update; Type: RULE; Schema: public; Owner: postgres
--

CREATE RULE geometry_columns_update AS ON UPDATE TO geometry_columns DO INSTEAD NOTHING;


SET search_path = job, pg_catalog;

--
-- Name: status_processid_fkey; Type: FK CONSTRAINT; Schema: job; Owner: postgres
--

ALTER TABLE ONLY status
    ADD CONSTRAINT status_processid_fkey FOREIGN KEY (processid) REFERENCES process(id);


SET search_path = public, pg_catalog;

--
-- Name: district_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY senator
    ADD CONSTRAINT district_fk FOREIGN KEY (district) REFERENCES senate(district);


--
-- PostgreSQL database dump complete
--

