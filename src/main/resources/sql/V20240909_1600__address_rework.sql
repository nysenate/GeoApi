ALTER TABLE geocoder.cache.geocache
ADD COLUMN bldg_id text;

UPDATE geocoder.cache.geocache
SET bldg_id = bldgnum::text;

CREATE FUNCTION orderParts(street text, streettype text)
    RETURNS TEXT AS $$
BEGIN
    IF streettype SIMILAR TO ('CAM|(CO|STATE|US) %|EXPY|% RD|FWY|HWY|RTE|TPKE') THEN
        RETURN streettype || ' ' || street;
    ELSIF streettype = 'I-' THEN
        RETURN streettype || street;
    ELSE
        RETURN street || ' ' || streettype;
    END IF;
END;
$$ LANGUAGE plpgsql;

UPDATE geocoder.cache.geocache
SET street = regexp_replace(
        trim(array_to_string(ARRAY[predir, orderParts(street, streettype), postdir], ' ')),
    ' {2,}', ' ');

DROP FUNCTION orderParts(street text, streettype text);

ALTER TABLE geocoder.cache.geocache
DROP COLUMN bldgNum,
DROP COLUMN predir,
DROP COLUMN streettype,
DROP COLUMN postdir;

DELETE FROM geocoder.cache.geocache
WHERE method = 'YahooDao' OR street = '' OR
    zip5 = '' OR zip5 = '00000' OR zip5 NOT SIMILAR TO '[0-9]{5}' OR
    zip4 = '0000' OR zip4 NOT SIMILAR TO '[0-9]{4}';

UPDATE geocoder.cache.geocache
SET zip4 = NULL WHERE zip4 = '';

DELETE FROM geocoder.cache.geocache
WHERE state NOT IN('AL', 'AK', 'AS', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'DC', 'FM', 'FL', 'GA', 'GU',
    'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MH', 'MD', 'MA', 'MI', 'MN',
    'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ', 'NM', 'NY', 'NC', 'ND', 'MP', 'OH', 'OK',
    'OR', 'PW', 'PA', 'PR', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VI', 'VA', 'WA', 'WV',
    'WI', 'WY');

CREATE FUNCTION isZip(baseZip text, nums int)
    RETURNS BOOLEAN AS $$
BEGIN
    RETURN baseZip != REPEAT('0', nums) AND baseZip SIMILAR TO REPEAT('[0-9]', nums);
END;
$$ LANGUAGE plpgsql;

ALTER TABLE geocoder.cache.geocache
    ADD CONSTRAINT valid_bldg_id CHECK (bldg_id IS NOT NULL AND bldg_id SIMILAR TO '[0-9]+%'),
    ADD CONSTRAINT valid_zips CHECK (
        (zip5 IS NOT NULL AND isZip(zip5, 5)) AND (zip4 IS NULL OR isZip(zip4, 4))
    ), ALTER COLUMN street SET NOT NULL,
    ADD CONSTRAINT valid_state CHECK (state IN
        ('AL', 'AK', 'AS', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'DC', 'FM', 'FL', 'GA', 'GU',
        'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MH', 'MD', 'MA', 'MI', 'MN',
        'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ', 'NM', 'NY', 'NC', 'ND', 'MP', 'OH', 'OK',
        'OR', 'PW', 'PA', 'PR', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VI', 'VA', 'WA', 'WV',
        'WI', 'WY'));

ALTER TABLE geocoder.cache.geocache RENAME COLUMN location TO postal_city;

UPDATE geocoder.cache.geocache
SET method = 'NYSGEO'
WHERE method = 'HttpNYSGeoDao' OR method = 'NYS Geo DB';

UPDATE geocoder.cache.geocache
SET method = 'GOOGLE'
WHERE method = 'HttpGoogleDao';

TRUNCATE TABLE public.streetfile;
TRUNCATE TABLE public.post_office;

ALTER TABLE public.streetfile
ALTER COLUMN zip5 DROP NOT NULL,
ALTER COLUMN zip5 TYPE varchar(5),
ADD CONSTRAINT valid_zip CHECK ( zip5 IS NOT NULL AND isZip(zip5, 5) );

ALTER TABLE public.post_office
ALTER COLUMN delivery_zip DROP NOT NULL,
ALTER COLUMN delivery_zip TYPE varchar(5),
ALTER COLUMN zip5 DROP NOT NULL,
ALTER COLUMN zip5 TYPE varchar(5),
ALTER COLUMN zip4 DROP NOT NULL,
ALTER COLUMN zip4 TYPE varchar(4),
ADD CONSTRAINT valid_zips CHECK (
    (delivery_zip IS NOT NULL AND isZip(delivery_zip, 5)) AND
    (zip5 IS NOT NULL AND isZip(zip5, 5)) AND
    (zip4 IS NOT NULL AND isZip(zip4, 4))
);
