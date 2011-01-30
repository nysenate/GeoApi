USE geoapi;

CREATE TABLE bulkrequest (
	requestTime BIGINT,
	grequests INT,
	yrequests INT,
	brequests INT
) ENGINE=INNODB;

CREATE TABLE jobprocess (
	id INT NOT NULL AUTO_INCREMENT,
	contact VARCHAR(128),
	jobtype VARCHAR(128),
	filename VARCHAR(512),
	requestTime BIGINT,
	segment INT,
	linecount INT,
	PRIMARY KEY(id)
) ENGINE=INNODB;