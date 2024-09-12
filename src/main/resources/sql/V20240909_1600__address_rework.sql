--TODO: add constraint for number
ALTER TABLE geocoder.cache.geocache
ADD COLUMN bldgId text,
ADD COLUMN street text;

UPDATE geocoder.cache.geocache
SET bldgId = bldgnum::text;

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
DROP COLUMN state,
DROP COLUMN predir,
DROP COLUMN street,
DROP COLUMN streettype,
DROP COLUMN postdir;

ALTER TABLE geocoder.cache.geocache
ALTER COLUMN zip5 TYPE integer
    USING (NULLIF(zip5, '')::integer),
ALTER COLUMN zip4 TYPE smallint
    USING (NULLIF(zip4, '')::smallint);

--TODO: zip5 should not be null
ALTER TABLE geocoder.cache.geocache
ADD CONSTRAINT validZips CHECK (
    (zip5 IS NULL OR (zip5 > 0 AND zip5 < 100000)) AND
    (zip4 IS NULL OR (zip4 > 0 AND zip4 < 10000))
);


