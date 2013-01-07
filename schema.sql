-- DROP TABLE IF EXISTS metric;
-- DROP TABLE IF EXISTS apiuser;
-- DROP TABLE IF EXISTS jobprocess;
-- DROP TABLE IF EXISTS assembly;
-- DROP TABLE IF EXISTS congressional;
-- DROP TABLE IF EXISTS senate;
-- DROP TABLE IF EXISTS `member`;
-- DROP TABLE IF EXISTS senator;
-- DROP TABLE IF EXISTS social;
-- DROP TABLE IF EXISTS office;
-- DROP TABLE IF EXISTS street_data;

CREATE TABLE apiuser (
  id int PRIMARY KEY AUTO_INCREMENT,
  apikey varchar(255) UNIQUE NOT NULL,
  name varchar(255) NOT NULL,
  description text
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE metric (
  command text(2056) DEFAULT NULL,
  `date` varchar(255) DEFAULT NULL,
  `host` varchar(255) DEFAULT NULL,
  userid int,
	FOREIGN KEY (userid) REFERENCES apiuser (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE jobprocess (
  id int PRIMARY KEY AUTO_INCREMENT,
  contact varchar(128) DEFAULT NULL,
  className text(512) DEFAULT NULL,
  filename text(512) DEFAULT NULL,
  requestTime bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


CREATE TABLE assembly (
  district varchar(255) PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE congressional (
  district varchar(255) PRIMARY KEY
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE senate (
  district varchar(50) PRIMARY KEY,
  districturl varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE senator (
  name varchar(255) DEFAULT NULL,
  contact varchar(255) DEFAULT NULL,
  url varchar(255) DEFAULT NULL,
  imageurl varchar(255) DEFAULT NULL,
  district varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE social (
  facebook text(1024) DEFAULT NULL,
  twitter text(1024) DEFAULT NULL,
  youtube text(1024) DEFAULT NULL,
  flickr text(1024) DEFAULT NULL,
  rss text(1024) DEFAULT NULL,
  contact varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `member` (
  name varchar(255) DEFAULT NULL,
  url varchar(255) DEFAULT NULL,
  district varchar(50) DEFAULT NULL,
  type int(2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE office (
  id int(11) PRIMARY KEY AUTO_INCREMENT,
  street text(1024) DEFAULT NULL,
  city text(255) DEFAULT NULL,
  state text(255) DEFAULT NULL,
  zip text(32) DEFAULT NULL,
  lat double(15,12) DEFAULT NULL,
  lon double(15,12) DEFAULT NULL,
  officeName varchar(255) DEFAULT NULL,
  phone varchar(255) DEFAULT NULL,
  fax varchar(255) DEFAULT NULL,
  contact varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE street_data (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  street varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  town varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  state varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  zip5 int(5) unsigned DEFAULT NULL,
  bldg_lo_num int(10) unsigned DEFAULT NULL,
  bldg_lo_chr varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  bldg_hi_num int(10) unsigned DEFAULT NULL,
  bldg_hi_chr varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  bldg_parity enum('ODDS','EVENS','ALL') COLLATE utf8_unicode_ci DEFAULT NULL,
  apt_lo_num int(10) unsigned DEFAULT NULL,
  apt_lo_chr varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  apt_hi_num int(10) unsigned DEFAULT NULL,
  apt_hi_chr varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  apt_parity enum('ODDS','EVENS','ALL') COLLATE utf8_unicode_ci DEFAULT NULL,
  election_code int(10) unsigned DEFAULT NULL,
  county_code int(10) unsigned DEFAULT NULL,
  assembly_code int(10) unsigned DEFAULT NULL,
  senate_code int(10) unsigned DEFAULT NULL,
  congressional_code int(10) unsigned DEFAULT NULL,
  town_code varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  senate_town_code varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  ward_code varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  school_code varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  senate_school_code smallint unsigned DEFAULT NULL,
  cleg_code int(10) DEFAULT NULL,
  cc_code int(10) DEFAULT NULL,
  fire_code varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  city_code varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  vill_code varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (id),
  KEY street (street),
  KEY zip5 (zip5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE street_data_map (
  map_col varchar(255) NOT NULL,
  county_code int(10) unsigned NOT NULL,
  from_code varchar(255) NOT NULL,
  to_code varchar(255) NOT NULL,
  UNIQUE KEY map_col (map_col,key_name,key_value,from_code)
) ENGINE=InnoDB CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Map street data columns to senate preferred values.';