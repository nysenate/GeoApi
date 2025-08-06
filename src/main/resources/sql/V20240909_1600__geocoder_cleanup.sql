ALTER TABLE geocoder.cache.geocache
SET SCHEMA public;

DELETE FROM public.geocache
WHERE bldgnum = 0;

ALTER TABLE public.geocache
    ALTER COLUMN bldgnum TYPE text;

ALTER TABLE public.geocache
    RENAME COLUMN bldgnum TO bldg_id;

DELETE FROM public.geocache
WHERE street = '' OR street LIKE '%[%' OR street LIKE '%]%';

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

ALTER TABLE public.geocache
DROP CONSTRAINT geocache_bldgnum_predir_street_streettype_postdir_location__key;

UPDATE public.geocache
SET street = regexp_replace(
        trim(array_to_string(ARRAY[predir, orderParts(street, streettype), postdir], ' ')),
    ' {2,}', ' ');

DROP FUNCTION orderParts(street text, streettype text);

ALTER TABLE public.geocache
DROP COLUMN predir,
DROP COLUMN streettype,
DROP COLUMN postdir;

UPDATE public.geocache
SET zip4 = NULL
WHERE zip4 = '';

DELETE FROM public.geocache
WHERE zip5 = '00000' OR zip5 NOT SIMILAR TO '[0-9]{5}' OR
    zip4 = '0000' OR (zip4 IS NOT NULL AND zip4 NOT SIMILAR TO '[0-9]{4}');

DELETE FROM public.geocache
WHERE state NOT IN('AL', 'AK', 'AS', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'DC', 'FM', 'FL', 'GA', 'GU',
    'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MH', 'MD', 'MA', 'MI', 'MN',
    'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ', 'NM', 'NY', 'NC', 'ND', 'MP', 'OH', 'OK',
    'OR', 'PW', 'PA', 'PR', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VI', 'VA', 'WA', 'WV',
    'WI', 'WY');

UPDATE public.geocache
SET method = 'NYSGEO'
WHERE method = 'HttpNYSGeoDao' OR method = 'NYS Geo DB';

UPDATE public.geocache
SET method = 'GOOGLE'
WHERE method = 'HttpGoogleDao';

DELETE FROM public.geocache
WHERE method != 'NYSGEO' AND method != 'GOOGLE';

ALTER TABLE public.geocache
RENAME COLUMN location TO postal_city;

DELETE FROM public.geocache a
WHERE EXISTS (
    SELECT 1
    FROM public.geocache b
    WHERE a.bldg_id = b.bldg_id
      AND a.street = b.street
      AND a.postal_city = b.postal_city
      AND a.state = b.state
      AND a.zip5 = b.zip5
      AND a.zip4 = b.zip4
      AND a.id < b.id
);

ALTER TABLE public.geocache
    ADD CONSTRAINT address_key
        UNIQUE (bldg_id, street, postal_city, state, zip5, zip4),
    ADD CONSTRAINT valid_bldg_id CHECK (bldg_id SIMILAR TO '[0-9]%'),
    ALTER COLUMN street SET NOT NULL,
    ADD CONSTRAINT valid_state CHECK (state IN
        ('AL', 'AK', 'AS', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'DC', 'FM', 'FL', 'GA', 'GU',
        'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MH', 'MD', 'MA', 'MI', 'MN',
        'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ', 'NM', 'NY', 'NC', 'ND', 'MP', 'OH', 'OK',
        'OR', 'PW', 'PA', 'PR', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VI', 'VA', 'WA', 'WV',
        'WI', 'WY')
    );
