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
