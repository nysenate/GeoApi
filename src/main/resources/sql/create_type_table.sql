CREATE TABLE districts.type_info (
    type_name TEXT PRIMARY KEY NOT NULL,
    code_column TEXT NOT NULL,
    name_column TEXT DEFAULT NULL,
    updated TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION districts.set_type_info_updated()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER type_info_set_updated
BEFORE UPDATE ON districts.type_info
FOR EACH ROW
EXECUTE FUNCTION districts.set_type_info_updated();

INSERT INTO districts.type_info VALUES
    ('senate', 'district'), ('assembly', 'district'),
    ('congressional', 'district'), ('zip', 'zip_code');

INSERT INTO districts.type_info VALUES
    ('county', 'senate_code', 'name'), ('town_city', 'abbrev', 'name'),
    ('school', 'tfcode', 'name'), ('electric_utility', 'gid', 'name');
