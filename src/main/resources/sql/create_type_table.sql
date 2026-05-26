CREATE TABLE districts.type_info (
    type_name TEXT PRIMARY KEY NOT NULL,
    code_column TEXT NOT NULL,
    name_column TEXT DEFAULT NULL,
    updated TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO districts.type_info VALUES
    ('senate', 'district'), ('assembly', 'district'),
    ('congressional', 'district'), ('zip', 'zip_code');

INSERT INTO districts.type_info VALUES
    ('county', 'senate_code', 'name'), ('town_city', 'abbrev', 'name'),
    ('school', 'tfcode', 'name'), ('electric_utility', 'gid', 'name');
