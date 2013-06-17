--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.9
-- Dumped by pg_dump version 9.1.9
-- Started on 2013-06-05 12:13:31 EDT

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 9 (class 2615 OID 147392)
-- Name: log; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA log;


SET search_path = log, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 216 (class 1259 OID 147582)
-- Dependencies: 9
-- Name: addresses; Type: TABLE; Schema: log; Owner: -; Tablespace: 
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


--
-- TOC entry 215 (class 1259 OID 147580)
-- Dependencies: 9 216
-- Name: addresses_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE addresses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3132 (class 0 OID 0)
-- Dependencies: 215
-- Name: addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE addresses_id_seq OWNED BY addresses.id;


--
-- TOC entry 212 (class 1259 OID 147545)
-- Dependencies: 9
-- Name: apirequests; Type: TABLE; Schema: log; Owner: -; Tablespace: 
--

CREATE TABLE apirequests (
    id integer NOT NULL,
    ipaddress inet NOT NULL,
    apiuserid integer NOT NULL,
    version integer,
    requesttypeid integer,
    requesttime timestamp without time zone NOT NULL
);


--
-- TOC entry 211 (class 1259 OID 147543)
-- Dependencies: 9 212
-- Name: apirequests_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE apirequests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3133 (class 0 OID 0)
-- Dependencies: 211
-- Name: apirequests_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE apirequests_id_seq OWNED BY apirequests.id;


--
-- TOC entry 220 (class 1259 OID 147635)
-- Dependencies: 9
-- Name: districtrequests; Type: TABLE; Schema: log; Owner: -; Tablespace: 
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


--
-- TOC entry 219 (class 1259 OID 147633)
-- Dependencies: 220 9
-- Name: districtrequests_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE districtrequests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3134 (class 0 OID 0)
-- Dependencies: 219
-- Name: districtrequests_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE districtrequests_id_seq OWNED BY districtrequests.id;


--
-- TOC entry 222 (class 1259 OID 147651)
-- Dependencies: 9
-- Name: districtresults; Type: TABLE; Schema: log; Owner: -; Tablespace: 
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


--
-- TOC entry 221 (class 1259 OID 147649)
-- Dependencies: 222 9
-- Name: districtresults_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE districtresults_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3135 (class 0 OID 0)
-- Dependencies: 221
-- Name: districtresults_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE districtresults_id_seq OWNED BY districtresults.id;


--
-- TOC entry 214 (class 1259 OID 147574)
-- Dependencies: 9
-- Name: geocoderequests; Type: TABLE; Schema: log; Owner: -; Tablespace: 
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


--
-- TOC entry 213 (class 1259 OID 147572)
-- Dependencies: 9 214
-- Name: geocoderequests_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE geocoderequests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3136 (class 0 OID 0)
-- Dependencies: 213
-- Name: geocoderequests_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE geocoderequests_id_seq OWNED BY geocoderequests.id;


--
-- TOC entry 218 (class 1259 OID 147604)
-- Dependencies: 1441 9
-- Name: geocoderesults; Type: TABLE; Schema: log; Owner: -; Tablespace: 
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


--
-- TOC entry 217 (class 1259 OID 147602)
-- Dependencies: 218 9
-- Name: geocoderesults_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE geocoderesults_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3137 (class 0 OID 0)
-- Dependencies: 217
-- Name: geocoderesults_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE geocoderesults_id_seq OWNED BY geocoderesults.id;


--
-- TOC entry 210 (class 1259 OID 147508)
-- Dependencies: 9
-- Name: requesttypes; Type: TABLE; Schema: log; Owner: -; Tablespace: 
--

CREATE TABLE requesttypes (
    id integer NOT NULL,
    serviceid integer NOT NULL,
    name character varying(24)
);


--
-- TOC entry 209 (class 1259 OID 147506)
-- Dependencies: 210 9
-- Name: requesttypes_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE requesttypes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3138 (class 0 OID 0)
-- Dependencies: 209
-- Name: requesttypes_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE requesttypes_id_seq OWNED BY requesttypes.id;


--
-- TOC entry 207 (class 1259 OID 147402)
-- Dependencies: 9
-- Name: services; Type: TABLE; Schema: log; Owner: -; Tablespace: 
--

CREATE TABLE services (
    id integer NOT NULL,
    name character varying(12)
);


--
-- TOC entry 208 (class 1259 OID 147408)
-- Dependencies: 9 207
-- Name: services_id_seq; Type: SEQUENCE; Schema: log; Owner: -
--

CREATE SEQUENCE services_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 3139 (class 0 OID 0)
-- Dependencies: 208
-- Name: services_id_seq; Type: SEQUENCE OWNED BY; Schema: log; Owner: -
--

ALTER SEQUENCE services_id_seq OWNED BY services.id;


--
-- TOC entry 3083 (class 2604 OID 147585)
-- Dependencies: 216 215 216
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY addresses ALTER COLUMN id SET DEFAULT nextval('addresses_id_seq'::regclass);


--
-- TOC entry 3081 (class 2604 OID 147548)
-- Dependencies: 212 211 212
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY apirequests ALTER COLUMN id SET DEFAULT nextval('apirequests_id_seq'::regclass);


--
-- TOC entry 3085 (class 2604 OID 147638)
-- Dependencies: 219 220 220
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY districtrequests ALTER COLUMN id SET DEFAULT nextval('districtrequests_id_seq'::regclass);


--
-- TOC entry 3086 (class 2604 OID 147654)
-- Dependencies: 221 222 222
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY districtresults ALTER COLUMN id SET DEFAULT nextval('districtresults_id_seq'::regclass);


--
-- TOC entry 3082 (class 2604 OID 147577)
-- Dependencies: 213 214 214
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY geocoderequests ALTER COLUMN id SET DEFAULT nextval('geocoderequests_id_seq'::regclass);


--
-- TOC entry 3084 (class 2604 OID 147607)
-- Dependencies: 218 217 218
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY geocoderesults ALTER COLUMN id SET DEFAULT nextval('geocoderesults_id_seq'::regclass);


--
-- TOC entry 3080 (class 2604 OID 147511)
-- Dependencies: 210 209 210
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY requesttypes ALTER COLUMN id SET DEFAULT nextval('requesttypes_id_seq'::regclass);


--
-- TOC entry 3079 (class 2604 OID 147410)
-- Dependencies: 208 207
-- Name: id; Type: DEFAULT; Schema: log; Owner: -
--

ALTER TABLE ONLY services ALTER COLUMN id SET DEFAULT nextval('services_id_seq'::regclass);


--
-- TOC entry 3121 (class 0 OID 147582)
-- Dependencies: 216 3128
-- Data for Name: addresses; Type: TABLE DATA; Schema: log; Owner: -
--

COPY addresses (id, addr1, addr2, city, state, zip5, zip4) FROM stdin;
\.


--
-- TOC entry 3140 (class 0 OID 0)
-- Dependencies: 215
-- Name: addresses_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('addresses_id_seq', 99, true);


--
-- TOC entry 3117 (class 0 OID 147545)
-- Dependencies: 212 3128
-- Data for Name: apirequests; Type: TABLE DATA; Schema: log; Owner: -
--

COPY apirequests (id, ipaddress, apiuserid, version, requesttypeid, requesttime) FROM stdin;
27	127.0.0.1	9	2	4	2013-06-04 11:51:13.716104
28	127.0.0.1	9	2	6	2013-06-04 11:53:05.255885
29	127.0.0.1	9	2	6	2013-06-04 11:59:37.385532
30	127.0.0.1	9	2	6	2013-06-04 12:00:52.415488
31	127.0.0.1	9	2	6	2013-06-04 12:01:49.341966
32	127.0.0.1	9	2	6	2013-06-04 12:02:05.498888
33	127.0.0.1	9	2	6	2013-06-04 12:02:33.385235
34	127.0.0.1	9	2	6	2013-06-04 12:02:44.828647
35	127.0.0.1	9	2	6	2013-06-04 12:02:53.630768
36	127.0.0.1	9	2	6	2013-06-04 14:17:02.640904
37	127.0.0.1	9	2	6	2013-06-04 14:21:25.035168
38	127.0.0.1	9	2	6	2013-06-04 14:26:59.725656
39	127.0.0.1	9	2	6	2013-06-04 14:28:27.564825
40	127.0.0.1	9	2	6	2013-06-04 14:28:39.562396
41	127.0.0.1	9	2	6	2013-06-04 14:28:43.213095
42	127.0.0.1	9	2	6	2013-06-04 14:30:40.3405
43	127.0.0.1	9	2	6	2013-06-04 14:31:38.500659
44	127.0.0.1	9	2	6	2013-06-04 14:31:43.172573
45	127.0.0.1	9	2	6	2013-06-04 14:31:51.711758
46	127.0.0.1	9	2	6	2013-06-04 14:32:15.610908
47	127.0.0.1	9	2	6	2013-06-04 14:32:31.661131
48	127.0.0.1	9	2	6	2013-06-04 14:32:51.844178
49	127.0.0.1	9	2	6	2013-06-04 14:34:33.369455
50	127.0.0.1	9	2	6	2013-06-04 16:52:45.763556
51	127.0.0.1	9	2	6	2013-06-04 16:52:56.961258
52	127.0.0.1	9	2	6	2013-06-04 16:53:23.399015
53	127.0.0.1	9	2	6	2013-06-04 16:53:35.32487
54	127.0.0.1	9	2	6	2013-06-04 16:58:51.022116
55	127.0.0.1	9	2	6	2013-06-04 17:01:39.895451
56	127.0.0.1	9	2	6	2013-06-05 11:00:27.339651
57	127.0.0.1	9	2	4	2013-06-05 11:41:28.156916
58	127.0.0.1	9	2	4	2013-06-05 11:49:19.430899
59	127.0.0.1	9	2	4	2013-06-05 11:49:22.857895
60	127.0.0.1	11	2	4	2013-06-05 11:50:35.234096
\.


--
-- TOC entry 3141 (class 0 OID 0)
-- Dependencies: 211
-- Name: apirequests_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('apirequests_id_seq', 60, true);


--
-- TOC entry 3125 (class 0 OID 147635)
-- Dependencies: 220 3128
-- Data for Name: districtrequests; Type: TABLE DATA; Schema: log; Owner: -
--

COPY districtrequests (id, apirequestid, addressid, provider, geoprovider, showmembers, showmaps, uspsvalidate, skipgeocode, districtstrategy, requesttime) FROM stdin;
1	37	36	\N	\N	t	t	f	f	\N	\N
2	39	42	\N	\N	t	t	f	f	\N	2013-06-04 14:28:28.026
3	40	45	\N	\N	t	t	f	f	\N	2013-06-04 14:28:39.843
4	41	48	\N	\N	t	t	f	f	\N	2013-06-04 14:28:43.233
5	42	51	\N	\N	t	t	f	f	\N	2013-06-04 14:30:40.733
6	43	55	\N	\N	f	f	f	f	\N	2013-06-04 14:31:43.722
7	44	57	\N	\N	f	f	f	f	\N	2013-06-04 14:31:43.735
8	45	60	\N	\N	f	f	f	f	\N	2013-06-04 14:31:51.911
9	46	63	streetfile	\N	f	f	f	f	\N	2013-06-04 14:32:15.958
10	47	64	streetfile	\N	f	f	f	t	\N	2013-06-04 14:32:31.662
11	48	67	streetfile	\N	t	f	f	f	\N	2013-06-04 14:32:51.855
12	49	70	\N	\N	f	f	f	f	\N	2013-06-04 14:34:33.381
13	50	73	\N	\N	t	t	f	f	\N	2013-06-04 16:52:48.72
14	51	76	\N	\N	t	t	f	f	\N	2013-06-04 16:52:56.982
15	52	79	\N	\N	t	t	f	f	\N	2013-06-04 16:53:23.61
16	53	82	\N	\N	t	t	f	f	\N	2013-06-04 16:53:35.336
17	54	85	\N	\N	f	f	f	f	\N	2013-06-04 16:58:51.244
18	55	88	\N	\N	t	t	f	f	\N	2013-06-04 17:01:40.194
19	56	91	\N	\N	t	t	f	f	\N	2013-06-05 11:00:28
\.


--
-- TOC entry 3142 (class 0 OID 0)
-- Dependencies: 219
-- Name: districtrequests_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('districtrequests_id_seq', 19, true);


--
-- TOC entry 3127 (class 0 OID 147651)
-- Dependencies: 222 3128
-- Data for Name: districtresults; Type: TABLE DATA; Schema: log; Owner: -
--

COPY districtresults (id, districtrequestid, assigned, status, senatecode, assemblycode, congressionalcode, countycode, resulttime) FROM stdin;
\.


--
-- TOC entry 3143 (class 0 OID 0)
-- Dependencies: 221
-- Name: districtresults_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('districtresults_id_seq', 1, false);


--
-- TOC entry 3119 (class 0 OID 147574)
-- Dependencies: 214 3128
-- Data for Name: geocoderequests; Type: TABLE DATA; Schema: log; Owner: -
--

COPY geocoderequests (id, apirequestid, addressid, provider, usefallback, usecache, requesttime) FROM stdin;
1	14	1	yahoo	t	f	2013-06-04 09:39:45.244
2	15	2	\N	t	t	2013-06-04 09:42:42.903
3	16	3	\N	t	t	2013-06-04 09:42:51.982
4	22	4	\N	t	t	2013-06-04 11:42:08.621
5	23	6	\N	t	t	2013-06-04 11:42:24.161
6	24	8	\N	t	t	2013-06-04 11:43:32.026
7	25	10	\N	t	t	2013-06-04 11:44:16.731
8	26	12	\N	t	t	2013-06-04 11:48:03.052
9	27	14	\N	t	t	2013-06-04 11:51:14.066
10	28	16	\N	t	t	2013-06-04 11:53:05.642
11	29	18	\N	t	t	2013-06-04 11:59:37.529
12	30	20	\N	t	t	2013-06-04 12:00:52.423
13	31	22	\N	t	t	2013-06-04 12:01:49.348
14	32	24	\N	t	t	2013-06-04 12:02:05.505
15	33	26	\N	t	t	2013-06-04 12:02:33.391
16	34	28	\N	t	t	2013-06-04 12:02:45.037
17	35	30	\N	t	t	2013-06-04 12:02:53.636
18	36	32	\N	t	t	2013-06-04 14:17:03.075
19	37	34	\N	t	t	2013-06-04 14:21:25.49
20	38	37	\N	t	t	2013-06-04 14:27:00.037
21	39	40	\N	t	t	2013-06-04 14:28:28.019
22	40	43	\N	t	t	2013-06-04 14:28:39.834
23	41	46	\N	t	t	2013-06-04 14:28:43.223
24	42	49	\N	t	t	2013-06-04 14:30:40.725
25	43	52	\N	t	t	2013-06-04 14:31:43.715
26	44	54	\N	t	t	2013-06-04 14:31:43.718
27	45	58	\N	t	t	2013-06-04 14:31:51.903
28	46	61	streetfile	t	t	2013-06-04 14:32:15.953
29	48	65	streetfile	t	t	2013-06-04 14:32:51.849
30	49	68	\N	t	t	2013-06-04 14:34:33.374
31	50	71	\N	t	t	2013-06-04 16:52:48.715
32	51	74	\N	t	t	2013-06-04 16:52:56.971
33	52	77	\N	t	t	2013-06-04 16:53:23.602
34	53	80	\N	t	t	2013-06-04 16:53:35.33
35	54	83	\N	t	t	2013-06-04 16:58:51.236
36	55	86	\N	t	t	2013-06-04 17:01:40.186
37	56	89	\N	t	t	2013-06-05 11:00:27.955
38	57	92	mapquest	t	f	2013-06-05 11:41:28.72
39	58	94	mapquest	t	f	2013-06-05 11:49:20.143
40	59	96	mapquest	t	f	2013-06-05 11:49:23.078
41	60	98	mapquest	t	f	2013-06-05 11:50:35.593
\.


--
-- TOC entry 3144 (class 0 OID 0)
-- Dependencies: 213
-- Name: geocoderequests_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('geocoderequests_id_seq', 41, true);


--
-- TOC entry 3123 (class 0 OID 147604)
-- Dependencies: 218 3128
-- Data for Name: geocoderesults; Type: TABLE DATA; Schema: log; Owner: -
--

COPY geocoderesults (id, geocoderequestid, success, addressid, method, quality, latlon, resulttime) FROM stdin;
\.


--
-- TOC entry 3145 (class 0 OID 0)
-- Dependencies: 217
-- Name: geocoderesults_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('geocoderesults_id_seq', 36, true);


--
-- TOC entry 3115 (class 0 OID 147508)
-- Dependencies: 210 3128
-- Data for Name: requesttypes; Type: TABLE DATA; Schema: log; Owner: -
--

COPY requesttypes (id, serviceid, name) FROM stdin;
1	1	validate
2	1	citystate
3	1	zipcode
4	2	geocode
5	2	revgeocode
6	3	assign
7	3	bluebird
8	4	lookup
9	5	senate
10	5	assembly
11	5	congressional
12	5	county
13	5	town
14	5	school
\.


--
-- TOC entry 3146 (class 0 OID 0)
-- Dependencies: 209
-- Name: requesttypes_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('requesttypes_id_seq', 14, true);


--
-- TOC entry 3112 (class 0 OID 147402)
-- Dependencies: 207 3128
-- Data for Name: services; Type: TABLE DATA; Schema: log; Owner: -
--

COPY services (id, name) FROM stdin;
1	address
2	geo
3	district
4	street
5	map
\.


--
-- TOC entry 3147 (class 0 OID 0)
-- Dependencies: 208
-- Name: services_id_seq; Type: SEQUENCE SET; Schema: log; Owner: -
--

SELECT pg_catalog.setval('services_id_seq', 5, true);


--
-- TOC entry 3100 (class 2606 OID 147590)
-- Dependencies: 216 216 3129
-- Name: addresses_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);


--
-- TOC entry 3095 (class 2606 OID 147553)
-- Dependencies: 212 212 3129
-- Name: apiRequests_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY apirequests
    ADD CONSTRAINT "apiRequests_pkey" PRIMARY KEY (id);


--
-- TOC entry 3104 (class 2606 OID 147640)
-- Dependencies: 220 220 3129
-- Name: districtRequests_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY districtrequests
    ADD CONSTRAINT "districtRequests_pkey" PRIMARY KEY (id);


--
-- TOC entry 3106 (class 2606 OID 147656)
-- Dependencies: 222 222 3129
-- Name: districtResults_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY districtresults
    ADD CONSTRAINT "districtResults_pkey" PRIMARY KEY (id);


--
-- TOC entry 3098 (class 2606 OID 147579)
-- Dependencies: 214 214 3129
-- Name: geocodeRequests_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY geocoderequests
    ADD CONSTRAINT "geocodeRequests_pkey" PRIMARY KEY (id);


--
-- TOC entry 3102 (class 2606 OID 147612)
-- Dependencies: 218 218 3129
-- Name: geocoderesults_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY geocoderesults
    ADD CONSTRAINT geocoderesults_pkey PRIMARY KEY (id);


--
-- TOC entry 3092 (class 2606 OID 147513)
-- Dependencies: 210 210 3129
-- Name: requestTypes_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY requesttypes
    ADD CONSTRAINT "requestTypes_pkey" PRIMARY KEY (id);


--
-- TOC entry 3089 (class 2606 OID 147415)
-- Dependencies: 207 207 3129
-- Name: services_pkey; Type: CONSTRAINT; Schema: log; Owner: -; Tablespace: 
--

ALTER TABLE ONLY services
    ADD CONSTRAINT services_pkey PRIMARY KEY (id);


--
-- TOC entry 3096 (class 1259 OID 147559)
-- Dependencies: 212 3129
-- Name: fki_requestType; Type: INDEX; Schema: log; Owner: -; Tablespace: 
--

CREATE INDEX "fki_requestType" ON apirequests USING btree (requesttypeid);


--
-- TOC entry 3090 (class 1259 OID 147519)
-- Dependencies: 210 3129
-- Name: fki_serviceIdIndex; Type: INDEX; Schema: log; Owner: -; Tablespace: 
--

CREATE INDEX "fki_serviceIdIndex" ON requesttypes USING btree (serviceid);


--
-- TOC entry 3093 (class 1259 OID 147520)
-- Dependencies: 210 210 3129
-- Name: requestTypes_serviceId_name_idx; Type: INDEX; Schema: log; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX "requestTypes_serviceId_name_idx" ON requesttypes USING btree (serviceid, name);


--
-- TOC entry 3087 (class 1259 OID 147416)
-- Dependencies: 207 3129
-- Name: services_name_idx; Type: INDEX; Schema: log; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX services_name_idx ON services USING btree (name);


--
-- TOC entry 3111 (class 2606 OID 147657)
-- Dependencies: 3103 222 220 3129
-- Name: districtRequestId_fkey; Type: FK CONSTRAINT; Schema: log; Owner: -
--

ALTER TABLE ONLY districtresults
    ADD CONSTRAINT "districtRequestId_fkey" FOREIGN KEY (districtrequestid) REFERENCES districtrequests(id);


--
-- TOC entry 3109 (class 2606 OID 147613)
-- Dependencies: 216 3099 218 3129
-- Name: geocoderesults_addressid_fkey; Type: FK CONSTRAINT; Schema: log; Owner: -
--

ALTER TABLE ONLY geocoderesults
    ADD CONSTRAINT geocoderesults_addressid_fkey FOREIGN KEY (addressid) REFERENCES addresses(id);


--
-- TOC entry 3110 (class 2606 OID 147618)
-- Dependencies: 3097 214 218 3129
-- Name: geocoderesults_geocoderequestid_fkey; Type: FK CONSTRAINT; Schema: log; Owner: -
--

ALTER TABLE ONLY geocoderesults
    ADD CONSTRAINT geocoderesults_geocoderequestid_fkey FOREIGN KEY (geocoderequestid) REFERENCES geocoderequests(id);


--
-- TOC entry 3108 (class 2606 OID 147554)
-- Dependencies: 212 210 3091 3129
-- Name: requestType; Type: FK CONSTRAINT; Schema: log; Owner: -
--

ALTER TABLE ONLY apirequests
    ADD CONSTRAINT "requestType" FOREIGN KEY (requesttypeid) REFERENCES requesttypes(id);


--
-- TOC entry 3107 (class 2606 OID 147514)
-- Dependencies: 210 207 3088 3129
-- Name: serviceIdIndex; Type: FK CONSTRAINT; Schema: log; Owner: -
--

ALTER TABLE ONLY requesttypes
    ADD CONSTRAINT "serviceIdIndex" FOREIGN KEY (serviceid) REFERENCES services(id);


-- Completed on 2013-06-05 12:13:31 EDT

--
-- PostgreSQL database dump complete
--

