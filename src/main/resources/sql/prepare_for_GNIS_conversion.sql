-- We're greatly simplifying this table.
ALTER TABLE public.county
    DROP COLUMN senate_code,
    DROP COLUMN fips_code,
    DROP COLUMN voterfile_code,
    DROP COLUMN streetfile_name,
    ADD CONSTRAINT county_pkey PRIMARY KEY (name);

-- Populate these code-independent fields to match on.
ALTER TABLE public.town_city
    ADD COLUMN county    text,
    ADD COLUMN muni_type text,
    ADD COLUMN name      text;

UPDATE public.town_city ptc
    SET county = dtc.county,
        muni_type = dtc.muni_type,
        name = dtc.name
FROM districts.town_city dtc
WHERE dtc.abbrev = ptc.district_code;

ALTER TABLE public.town_city
    DROP COLUMN district_code,
    ADD CONSTRAINT town_city_pkey PRIMARY KEY (county, muni_type, name);

--We now use a LEFT JOIN, so these entries are no longer needed.
DELETE FROM public.town_city
WHERE voterfile_code IS NULL;
