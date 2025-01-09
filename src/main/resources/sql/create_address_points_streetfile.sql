--1. Download the data as CSV from here: https://data.gis.ny.gov/datasets/dfa176b4cf284539812c05478dc028d2
--2. Unzip, and open the whole folder in QGIS.
--3. Right-click "AddressPoints_SAM" -> Export -> Save Features As -> select "ESRI Shapefile" -> name the file, and click Ok
--4. shp2pgsql -d -I -s 26918:4326 *.shp public.address_points > address_points.sql
--5. Log into geoapi and import the SQL.

ALTER TABLE public.address_points
DROP COLUMN objectid,
DROP COLUMN nysaddress,
DROP COLUMN countyid,
DROP COLUMN nysstreeti,
DROP COLUMN suffixaddr,
DROP COLUMN premodifie,
DROP COLUMN predirecti,
DROP COLUMN pretype,
DROP COLUMN separatore,
DROP COLUMN streetname,
DROP COLUMN posttype,
DROP COLUMN postdirect,
DROP COLUMN postmodifi,
DROP COLUMN subaddress,
DROP COLUMN structure,
DROP COLUMN floor,
DROP COLUMN unit,
DROP COLUMN location,
DROP COLUMN site,
DROP COLUMN subsite,
DROP COLUMN businessna,
DROP COLUMN zipname;

DELETE FROM public.address_points
WHERE state != 'NY'
    OR pointtype != '1';

ALTER TABLE public.address_points
DROP COLUMN state,
DROP COLUMN pointtype,
DROP COLUMN addresssou,
DROP COLUMN discrepanc,
DROP COLUMN dateupdate;

DELETE FROM public.address_points
WHERE primarypoi != 'Y';

ALTER TABLE public.address_points
DROP COLUMN primarypoi;

DELETE FROM public.address_points
WHERE placetype IS NOT NULL OR
      status != 'Active' OR country NOT ILIKE 'US' OR
      milepost IS NOT NULL;

ALTER TABLE public.address_points
DROP COLUMN placetype,
--TODO: contact them, perhaps more filtering on flags?
DROP COLUMN ap_flag,
DROP COLUMN status,
DROP COLUMN addresslab,
DROP COLUMN cldxf_pred,
DROP COLUMN cldxf_post,
DROP COLUMN cldxf_po_1,
DROP COLUMN country,
DROP COLUMN esn,
DROP COLUMN unincorpor,
DROP COLUMN neighborho,
DROP COLUMN msagcommun,
DROP COLUMN milepost,
DROP COLUMN swis_sbl_i,
DROP COLUMN swis_print,
DROP COLUMN addresstyp,
DROP COLUMN username,
DROP COLUMN cr_usernam,
DROP COLUMN cr_datetim,
DROP COLUMN at_usernam,
DROP COLUMN at_datetim,
DROP COLUMN sp_usernam,
DROP COLUMN sp_datetim;

UPDATE public.address_points
SET bldg_id = prefixaddr || bldg_id
WHERE prefixaddr IS NOT NULL;

ALTER TABLE public.address_points
ADD COLUMN bldg_id varchar;

UPDATE public.address_points
SET bldg_id = addressnum::varchar;

ALTER TABLE public.address_points
DROP COLUMN prefixaddr,
DROP COLUMN addressnum,
DROP COLUMN latitude,
DROP COLUMN longitude;

ALTER TABLE public.address_points
ADD COLUMN assembly_district int,
ADD COLUMN senate_district int,
ADD COLUMN congressional_district int;

--TODO: function that takes in geometry and assigns it to proper district.
-- Will need this anyways for automatic geocache assignment
