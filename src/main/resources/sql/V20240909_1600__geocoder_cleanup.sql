ALTER TABLE cache.geocache
SET SCHEMA public;

DELETE FROM public.geocache
WHERE bldgnum = 0;

ALTER TABLE public.geocache
    ALTER COLUMN bldgnum TYPE text;

DELETE FROM public.geocache
WHERE street NOT SIMILAR TO '([A-Z]|[0-9]| )+';

CREATE FUNCTION addOrdinalIndicators(street text)
    RETURNS TEXT AS $$
BEGIN
    IF street NOT SIMILAR TO '[0-9]+' THEN
        RETURN street;
    ELSIF street SIMILAR TO '1[1-3]+' THEN
        RETURN street || 'TH';
    ELSIF street SIMILAR TO '%1' THEN
        RETURN street || 'ST';
    ELSIF street SIMILAR TO '%2' THEN
        RETURN street || 'ND';
    ELSIF street SIMILAR TO '%3' THEN
        RETURN street || 'RD';
    ELSE
        RETURN street || 'TH';
    END IF;
END;
$$ LANGUAGE plpgsql;

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

ALTER TABLE public.geocache
ADD COLUMN primary_addr1 TEXT;

UPDATE public.geocache
SET primary_addr1 = regexp_replace(
        trim(array_to_string(ARRAY[bldgnum, predir, orderParts(addOrdinalIndicators(street), streettype), postdir], ' ')),
    ' {2,}', ' ');

DROP FUNCTION orderParts(street text, streettype text);
DROP FUNCTION addOrdinalIndicators(street text);

ALTER TABLE public.geocache
DROP COLUMN bldgnum,
DROP COLUMN predir,
DROP COLUMN street,
DROP COLUMN streettype,
DROP COLUMN postdir,
DROP COLUMN zip4;

DELETE FROM public.geocache
WHERE zip5 = '00000' OR zip5 NOT SIMILAR TO '[0-9]{5}';

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
    WHERE a.primary_addr1 = b.primary_addr1
      AND a.postal_city = b.postal_city
      AND a.state = b.state
      AND a.zip5 = b.zip5
      AND a.id < b.id
);

ALTER TABLE public.geocache
    ADD CONSTRAINT address_key
        UNIQUE (primary_addr1, postal_city, state, zip5),
    ADD CONSTRAINT valid_state CHECK (state IN
        ('AL', 'AK', 'AS', 'AZ', 'AR', 'CA', 'CO', 'CT', 'DE', 'DC', 'FM', 'FL', 'GA', 'GU',
        'HI', 'ID', 'IL', 'IN', 'IA', 'KS', 'KY', 'LA', 'ME', 'MH', 'MD', 'MA', 'MI', 'MN',
        'MS', 'MO', 'MT', 'NE', 'NV', 'NH', 'NJ', 'NM', 'NY', 'NC', 'ND', 'MP', 'OH', 'OK',
        'OR', 'PW', 'PA', 'PR', 'RI', 'SC', 'SD', 'TN', 'TX', 'UT', 'VI', 'VA', 'WA', 'WV',
        'WI', 'WY')
    );
