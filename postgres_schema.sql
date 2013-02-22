CREATE TYPE parity AS ENUM ('ODDS', 'EVENS', 'ALL');

CREATE TABLE apiuser
(
  id serial NOT NULL,
  apikey character varying,
  name character varying,
  description text,
  CONSTRAINT apiuser_id PRIMARY KEY (id ),
  CONSTRAINT apiuser_apikey_key UNIQUE (apikey )
);

CREATE TABLE streetfile
(
   id serial, 
   street character varying(255) COLLATE pg_catalog."en_US.utf8",
   town character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   state character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   zip5 integer DEFAULT NULL,
   bldg_lo_num integer DEFAULT NULL,
   bldg_lo_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   bldg_hi_num integer DEFAULT NULL,
   bldg_hi_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   bldg_parity parity DEFAULT NULL,
   apt_lo_num integer DEFAULT NULL,
   apt_lo_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   apt_hi_num integer DEFAULT NULL,
   apt_hi_chr character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   apt_parity parity DEFAULT NULL,
   election_code integer DEFAULT NULL,
   county_code integer DEFAULT NULL,
   assembly_code integer DEFAULT NULL,
   senate_code integer DEFAULT NULL,
   congressional_code integer DEFAULT NULL,
   boe_town_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   town_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   ward_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   boe_school_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   school_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   cleg_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   cc_code integer DEFAULT NULL,
   fire_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   city_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   vill_code character varying(255) COLLATE pg_catalog."en_US.utf8" DEFAULT NULL,
   CONSTRAINT streetfile_id PRIMARY KEY (id)   
) 
WITH (
  OIDS = FALSE
)
;
COMMENT ON TABLE streetfile
  IS 'Contains parsed BOE street file data for performing district lookups.';

CREATE INDEX ON streetfile (street);
CREATE INDEX ON streetfile (town);
CREATE INDEX ON streetfile (zip5);

