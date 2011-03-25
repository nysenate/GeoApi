USE geoapi;

CREATE TABLE jobprocess (
	id INT NOT NULL AUTO_INCREMENT,
	contact VARCHAR(128),
	className VARCHAR(512),
	filename VARCHAR(512),
	requestTime BIGINT,
	PRIMARY KEY(id)
) ENGINE=INNODB;