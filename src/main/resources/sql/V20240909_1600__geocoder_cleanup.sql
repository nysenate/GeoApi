ALTER TABLE geocoder.cache.geocache
SET SCHEMA public;

ALTER TABLE public.geocache
ADD COLUMN bldg_id text NOT NULL DEFAULT '';

UPDATE public.geocache
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

UPDATE public.geocache
SET street = regexp_replace(
        trim(array_to_string(ARRAY[predir, orderParts(street, streettype), postdir], ' ')),
    ' {2,}', ' ');

DROP FUNCTION orderParts(street text, streettype text);

ALTER TABLE public.geocache
DROP COLUMN bldgNum,
DROP COLUMN predir,
DROP COLUMN streettype,
DROP COLUMN postdir;

UPDATE public.geocache
SET zip4 = NULL
WHERE zip4 = '';

DELETE FROM public.geocache
WHERE method = 'YahooDao' OR street = '' OR street LIKE '%[%' OR street LIKE '%]%' OR
    zip5 = '00000' OR zip5 NOT SIMILAR TO '[0-9]{5}' OR
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

ALTER TABLE public.geocache RENAME COLUMN location TO postal_city;
ALTER TABLE public.geocache
    ADD CONSTRAINT valid_bldg_id CHECK (bldg_id SIMILAR TO '[0-9]%'),
    ALTER COLUMN street SET NOT NULL,
    ADD CONSTRAINT valid_state CHECK (state IN
        ('AL', 'AK', 'AS', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'DC', 'FM', 'FL', 'GA', 'GU',
        'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MH', 'MD', 'MA', 'MI', 'MN',
        'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ', 'NM', 'NY', 'NC', 'ND', 'MP', 'OH', 'OK',
        'OR', 'PW', 'PA', 'PR', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VI', 'VA', 'WA', 'WV',
        'WI', 'WY'));
