ALTER TABLE geoapi.public.county
ADD COLUMN link VARCHAR;

--Albany
UPDATE geoapi.public.county
SET link = 'https://www.albanycounty.com/departments/health'
WHERE id = 1;

--Allegany
UPDATE geoapi.public.county
SET link = 'https://www.alleganyco.com/health-department/'
WHERE id = 2;

--Broome
UPDATE geoapi.public.county
SET link = 'http://www.gobroomecounty.com/hd'
WHERE id = 3;

--Cattaraugus
UPDATE geoapi.public.county
SET link = 'https://www.cattco.org/health'
WHERE id = 4;

--Cayuga
UPDATE geoapi.public.county
SET link = 'https://www.cayugacounty.us/153/Health-Department'
WHERE id = 5;

--Chautauqua
UPDATE geoapi.public.county
SET link = 'https://chqgov.com/health-and-human-services/Health-Human-Services'
WHERE id = 6;

--Chemung
UPDATE geoapi.public.county
SET link = 'http://www.chemungcountyhealth.org/'
WHERE id = 7;

--Chenango
UPDATE geoapi.public.county
SET link = 'http://www.co.chenango.ny.us/public-health/'
WHERE id = 8;

--Clinton
UPDATE geoapi.public.county
SET link = 'http://www.clintonhealth.org/'
WHERE id = 9;

--Columbia
UPDATE geoapi.public.county
SET link = 'https://www.columbiacountynyhealth.com/'
WHERE id = 10;

--Cortland
UPDATE geoapi.public.county
SET link = 'https://www.cortland-co.org/432/Health-Department'
WHERE id = 11;

--Delaware
UPDATE geoapi.public.county
SET link = 'http://www.co.delaware.ny.us/departments/phn/phn.htm'
WHERE id = 12;

--Dutchess
UPDATE geoapi.public.county
SET link = 'https://www.dutchessny.gov/Departments/DBCH/dbch.htm'
WHERE id = 13;

--Erie
UPDATE geoapi.public.county
SET link = 'http://www2.erie.gov/health/'
WHERE id = 14;

--Essex
UPDATE geoapi.public.county
SET link = 'https://www.co.essex.ny.us/Health/'
WHERE id = 15;

--Franklin
UPDATE geoapi.public.county
SET link = 'https://countyfranklin.digitaltowpath.org:10078/content/Departments/View/2'
WHERE id = 16;

--Fulton
UPDATE geoapi.public.county
SET link = 'http://www.fultoncountyny.gov/public-health'
WHERE id = 17;

--Genesee
UPDATE geoapi.public.county
SET link = 'https://www.co.genesee.ny.us/departments/health/index.php'
WHERE id = 18;

--Greene
UPDATE geoapi.public.county
SET link = 'https://www.greenegovernment.com/departments/public-health'
WHERE id = 19;

--Hamilton
UPDATE geoapi.public.county
SET link = 'https://www.hamiltoncounty.com/health-human-services/public-health-home'
WHERE id = 20;

--Herkimer
UPDATE geoapi.public.county
SET link = 'https://www.herkimercounty.org/services-and-departments/public-health.php'
WHERE id = 21;

--Jefferson
UPDATE geoapi.public.county
SET link = 'https://co.jefferson.ny.us/departments/PublicHealth'
WHERE id = 22;

--Lewis
UPDATE geoapi.public.county
SET link = 'https://www.lewiscounty.org/departments/public-health/public-health'
WHERE id = 23;

--Livingston
UPDATE geoapi.public.county
SET link = 'https://www.livingstoncounty.us/172/Department-of-Health'
WHERE id = 24;

--Madison
UPDATE geoapi.public.county
SET link = 'https://www.madisoncounty.ny.gov/206/Health-Department'
WHERE id = 25;

--Monroe
UPDATE geoapi.public.county
SET link = 'https://www2.monroecounty.gov/health-index.php'
WHERE id = 26;

--Montgomery
UPDATE geoapi.public.county
SET link = 'https://www.co.montgomery.ny.us/web/sites/departments/publichealth/default.asp'
WHERE id = 27;

--Nassau
UPDATE geoapi.public.county
SET link = 'https://www.nassaucountyny.gov/agencies/Health/index.html'
WHERE id = 28;

--Niagara
UPDATE geoapi.public.county
SET link = 'https://www.niagaracounty.com/Health'
WHERE id = 29;

--Oneida
UPDATE geoapi.public.county
SET link = 'https://ocgov.net//oneida/health'
WHERE id = 30;

--Onondaga
UPDATE geoapi.public.county
SET link = 'http://www.ongov.net/health/index.html'
WHERE id = 31;

--Ontario
UPDATE geoapi.public.county
SET link = 'http://www.co.ontario.ny.us/101/Public-Health'
WHERE id = 32;

--Orange
UPDATE geoapi.public.county
SET link = 'https://www.orangecountygov.com/149/Health'
WHERE id = 33;

--Orleans
UPDATE geoapi.public.county
SET link = 'http://www.orleansny.com/Departments/Health'
WHERE id = 34;

--Oswego
UPDATE geoapi.public.county
SET link = 'https://health.oswegocounty.com/'
WHERE id = 35;

--Otsego
UPDATE geoapi.public.county
SET link = 'https://www.otsegocounty.com/departments/d-m/health_department/covid19.php'
WHERE id = 36;

--Putnam
UPDATE geoapi.public.county
SET link = 'https://www.putnamcountyny.com/health/'
WHERE id = 37;

--Rensselaer
UPDATE geoapi.public.county
SET link = 'https://www.rensco.com/departments/public-health/'
WHERE id = 38;

--Rockland
UPDATE geoapi.public.county
SET link = 'http://rocklandgov.com/departments/health'
WHERE id = 39;

--St. Lawrence
UPDATE geoapi.public.county
SET link = 'https://www.stlawco.org/Departments/PublicHealth/'
WHERE id = 40;

--Saratoga
UPDATE geoapi.public.county
SET link = 'https://www.saratogacountyny.gov/departments/publichealth/'
WHERE id = 41;

--Schenectady
UPDATE geoapi.public.county
SET link = 'https://www.schenectadycounty.com/publichealth'
WHERE id = 42;

--Schoharie
UPDATE geoapi.public.county
SET link = 'https://www4.schohariecounty-ny.gov/departments/public-health/'
WHERE id = 43;

--Schuyler
UPDATE geoapi.public.county
SET link = 'http://www.schuylercounty.us/166/Public-Health'
WHERE id = 44;

--Seneca
UPDATE geoapi.public.county
SET link = 'https://www.co.seneca.ny.us/departments/community-services/public-health/'
WHERE id = 45;

--Steuben
UPDATE geoapi.public.county
SET link = 'https://www.steubencony.org/Pages.asp?PGID=36'
WHERE id = 46;

--Suffolk
UPDATE geoapi.public.county
SET link = 'https://www.suffolkcountyny.gov/health'
WHERE id = 47;

--Sullivan
UPDATE geoapi.public.county
SET link = 'http://sullivanny.us/Departments/Publichealth'
WHERE id = 48;

--Tioga
UPDATE geoapi.public.county
SET link = 'http://www.tiogacountyny.com/departments/public-health/'
WHERE id = 49;

--Tompkins
UPDATE geoapi.public.county
SET link = 'https://tompkinscountyny.gov/health'
WHERE id = 50;

--Ulster
UPDATE geoapi.public.county
SET link = 'https://ulstercountyny.gov/health/health-mental-health'
WHERE id = 51;

--Warren
UPDATE geoapi.public.county
SET link = 'https://www.warrencountyny.gov/healthservices/'
WHERE id = 52;

--Washington
UPDATE geoapi.public.county
SET link = 'https://washingtoncountyny.gov/299/Public-Health-Services'
WHERE id = 53;

--Wayne
UPDATE geoapi.public.county
SET link = 'https://health.westchestergov.com/'
WHERE id = 54;

--Westchester
UPDATE geoapi.public.county
SET link = 'https://health.westchestergov.com/'
WHERE id = 55;

--Wyoming
UPDATE geoapi.public.county
SET link = 'http://www.wyomingco.net/203/Health-Department'
WHERE id = 56;

--Yates
UPDATE geoapi.public.county
SET link = 'https://www.yatescounty.org/211/Public-Health'
WHERE id = 57;

--Bronx
UPDATE geoapi.public.county
SET link = 'https://www1.nyc.gov/site/doh/health/health-topics/coronavirus.page'
WHERE id = 60;

--Kings
UPDATE geoapi.public.county
SET link = 'https://www1.nyc.gov/site/doh/health/health-topics/coronavirus.page'
WHERE id = 61;

--New York
UPDATE geoapi.public.county
SET link = 'https://www1.nyc.gov/site/doh/health/health-topics/coronavirus.page'
WHERE id = 62;

--Queens
UPDATE geoapi.public.county
SET link = 'https://www1.nyc.gov/site/doh/health/health-topics/coronavirus.page'
WHERE id = 63;

--Richmond https://www.vote.nyc.ny.us/html/voters/earlyVoting.shtml
UPDATE geoapi.public.county
SET link = 'https://www1.nyc.gov/site/doh/health/health-topics/coronavirus.page'
WHERE id = 64;