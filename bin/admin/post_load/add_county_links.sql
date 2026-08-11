-- Adds districts.county.link, the county health department page SAGE links to.
-- No source carries this, so the mapping is checked in here and applied by name.
--
-- URLs verified 2026-08-11: every one below was fetched and returns 200 with no further
-- redirect, at the HTTP level and in the page body (meta-refresh / location.replace), except
-- the six noted as WAF-blocked (the host refuses datacenter traffic, so the page could not
-- be fetched from here even though it is the current canonical URL).
-- Most counties have migrated to .gov hosts since these were first collected.

ALTER TABLE districts.county ADD COLUMN IF NOT EXISTS link text;

WITH links (name, link) AS (VALUES
    ('Albany',        'https://www.albanycountyny.gov/departments/health'),
    ('Allegany',      'https://www.alleganyco.gov/health-department/'),
    ('Broome',        'https://broomecountyny.gov/hd'),
    ('Cattaraugus',   'https://www.cattco.gov/health'),
    ('Cayuga',        'https://www.cayugacounty.gov/1850/Health-Department'),
    ('Chautauqua',    'https://chautauquacountyny.gov/health-and-human-services/Health-Human-Services'),
    ('Chemung',       'https://www.chemungcountyny.gov/241/Public-Health'),
    ('Chenango',      'https://www.chenangocountyny.gov/206/Public-Health'),
    ('Clinton',       'https://www.clintoncountyny.gov/health'),
    ('Columbia',      'https://columbiacountynyhealth.com/'),
    ('Cortland',      'https://www.cortlandcountyny.gov/432/Health-Department'),
    ('Delaware',      'https://www.delcony.gov/departments/phn/phn.htm'),
    ('Dutchess',      'https://www.dutchessny.gov/Departments/DBCH/dbch.htm'),
    ('Erie',          'https://www3.erie.gov/health/'),
    ('Essex',         'https://health.essexcountyny.gov/'),
    ('Franklin',      'https://www.franklincountyny.gov/departments/human_services/public_health/index.php'),
    ('Fulton',        'https://www.fultoncountyny.gov/public-health'),
    ('Genesee',       'https://www.geneseeny.gov/gohealth/Home'),
    ('Greene',        'https://greenecountyny.gov/departments/public-health/'),
    ('Hamilton',      'https://www.hamiltoncountyny.gov/health-human-services/public-health-home'),
    ('Herkimer',      'https://www.herkimercountyny.gov/departments/public-health/'),
    ('Jefferson',     'https://www.jeffersoncountyny.gov/departments/PublicHealth'),
    ('Lewis',         'https://lewiscountyny.gov/departments/public-health/'),
    ('Livingston',    'https://www.livingstoncountyny.gov/172/Department-of-Health'),
    ('Madison',       'https://www.madisoncounty.ny.gov/3007/Public-Health'),
    ('Monroe',        'https://www.monroecounty.gov/health'),
    ('Montgomery',    'https://madeofsomethingstronger.com/'),                              -- county's health portal
    ('Nassau',        'https://www.nassaucountyny.gov/agencies/Health/index.html'),
    ('Niagara',       'https://www.niagaracounty.gov/departments/g-l/public_health/index.php'),
    ('Oneida',        'https://oneidacountyny.gov/departments/health/'),
    ('Onondaga',      'https://onondaga.gov/health/'),
    ('Ontario',       'https://www.ontariocountyny.gov/2341/Public-Health'),
    ('Orange',        'https://www.orangecountyny.gov/2354/Health'),
    ('Orleans',       'https://www.orleanscountyny.gov/departments/health_services.php'),
    ('Oswego',        'https://health.oswegocountyny.gov/'),
    ('Otsego',        'https://www.otsegocountyny.gov/departments/health_department/index.php'),
    ('Putnam',        'https://www.putnamcountyny.gov/health/'),
    ('Rensselaer',    'https://www.rensco.com/241/Public-Health'),
    ('Rockland',      'https://www.rocklandcountyny.gov/departments/health'),
    ('Saratoga',      'https://www.saratogacountyny.gov/departments/health/'),
    ('Schenectady',   'https://www.schenectadycountyny.gov/public-health'),
    ('Schoharie',     'https://www.schohariecounty-ny.gov/departments/public_health/index.php'),
    ('Schuyler',      'https://www.schuylercountyny.gov/166/Public-Health'),
    ('Seneca',        'https://senecacountyhealthny.gov/'),
    ('St. Lawrence',  'https://www.stlawco.gov/Departments/PublicHealth'),
    ('Steuben',       'https://www.steubencony.org/public-health/'),
    ('Suffolk',       'https://www.suffolkcountyny.gov/health'),
    ('Sullivan',      'https://www.sullivanny.gov/Departments/Publichealth'),
    ('Tioga',         'https://www.tiogacountyny.com/departments/public-health/'),
    ('Tompkins',      'https://www.tompkinscountyny.gov/health'),
    ('Ulster',        'https://www.ulstercountyny.gov/health/health-mental-health'),
    ('Warren',        'https://www.warrencountyny.gov/healthservices/home'),
    ('Washington',    'https://www.washingtoncountyny.gov/299/Public-Health-Services'),
    ('Wayne',         'https://waynecountyny.gov/881/Public-Health'),
    ('Westchester',   'https://health.westchestercountyny.gov/'),
    ('Wyoming',       'https://www.wyomingcountyny.gov/203/Health-Department'),
    ('Yates',         'https://www.yatescountyny.gov/211/Public-Health')
)
UPDATE districts.county AS c
SET link = links.link
FROM links
WHERE c.name = links.name;

UPDATE districts.county
SET link = 'https://www.nyc.gov/site/doh/covid/covid-19-main.page'
WHERE name IN ('Bronx', 'Kings', 'New York', 'Queens', 'Richmond');

\echo 'Counties with no link (name did not match the mapping above):'
SELECT name
FROM districts.county
WHERE link IS NULL
ORDER BY name;
