-- The City of Buffalo's wards are its 9 named Common Council districts, and have no official numbers.
-- Erie County BOE data keys them by 3-letter abbreviation, so the codes here are the arbitrary ones
-- assigned in CompactDistrictMap#wardCorrectionMap.
CREATE TABLE IF NOT EXISTS public.buffalo_ward (
    abbrev VARCHAR(3) PRIMARY KEY,
    full_name VARCHAR NOT NULL,
    code SMALLINT NOT NULL
);

INSERT INTO public.buffalo_ward (abbrev, full_name, code)
VALUES ('DEL', 'Delaware', 10),
       ('ELL', 'Ellicott', 20),
       ('FIL', 'Fillmore', 30),
       ('LOV', 'Lovejoy', 40),
       ('MAS', 'Masten', 50),
       ('NIA', 'Niagara', 60),
       ('NOR', 'North', 70),
       ('SOU', 'South', 80),
       ('UNI', 'University', 90)
ON CONFLICT (abbrev) DO NOTHING;
