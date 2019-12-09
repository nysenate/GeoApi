create table manual_dataentry_geopoints
(
    zipcode text,
    type    text,
    lon    text,
    lat     text,
    source  text
);

alter table manual_dataentry_geopoints
    owner to geoadmin;