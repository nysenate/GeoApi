# AGENTS.md

This file provides guidance to AI coding agents working with code in this repository.

## Project Overview

SAGE (Senate Address Geo-coding Engine) is a Java web application for the NY State Senate that provides address verification/correction, geocoding, and district assignment. It uses USPS for address validation, NYS Geo and Google for geocoding, and Board of Election streetfiles/LATFOR shapefiles for district assignment.

## Build & Test Commands

This is a Maven project (Java 21, Spring 6, WAR packaging, deployed on Tomcat 11).

- **Build**: `mvn clean package`
- **Unit tests**: `mvn test` (runs tests annotated with `@UnitTest`)
- **Integration tests**: `mvn verify` (runs tests annotated with `@IntegrationTest` via failsafe)
- **Single test**: `mvn test -Dtest=TestClassName`
- **Skip tests on build**: `mvn package -DskipTests`

Test annotations are in `gov.nysenate.sage.annotation` — tests must be categorized as `@UnitTest` or `@IntegrationTest` to be picked up by surefire/failsafe respectively.

## Configuration

Copy `src/main/resources/app.properties.example` to `app.properties` and configure. Key settings include database credentials (PostgreSQL), API keys (USPS, Google), and provider rankings for geocoding/district assignment.

The `bin/admin/` scripts read their own `bin/admin/admin.script.properties`, copied from the `.example` beside it — `baseUrl` and `adminKey` for the endpoints they call, `database` and `db_user` for `psql`. Like `app.properties`, it stays untracked — `.gitignore` excludes `*.properties`, which is why only the `.example` is in the repo.

## Architecture

**Provider pipeline with ranked fallback**: Both geocoding and district assignment use a ranked provider pattern. Providers are tried in order (configured via `geocoder.ranking` and `district.ranking` in `app.properties`) until one succeeds.

- **Geocoding** (`provider/geocode/GeocodeService`): Geocoders are `GEOCACHE`, `NYSGEO`, `GOOGLE`. Results from non-cache providers are cached in `GeoCache` for future lookups.
- **District assignment** (`provider/district/DistrictService`): Sources (`LocalSource`) are `STREETFILE` (address-based lookup) and `SHAPEFILE` (geometry-based lookup). Results are consolidated across providers — streetfile may provide some district types, shapefile fills in the rest.
- **Address validation** (`provider/address/`): not ranked. `AddressSource` is `AMS` or `AIS`, both ultimately USPS — AMS is a separate USPS-AMS webapp SAGE calls over HTTP (`usps.ams.api.url`), AIS the free USPS AIS web service (`usps.ais.url`) — and `usps.default` picks one, currently `AMS`.

**Package layout** (under `gov.nysenate.sage`):
- `controller/` — Spring MVC controllers. `api/` has the REST API (`/api/v2/...`), `admin/` for admin endpoints, `job/` for batch job management, `map/` for the public embedded maps, `ui/` for the frontend shell, `interceptor/` for `PageSetupInterceptor`. The shell controller is `AngularAppCtrl`, which only picks which JSP in `src/main/webapp/WEB-INF/views` to return — the pages themselves are AngularJS templates (`ng-*` attributes and `{{ }}` bindings inline in the JSP), driven by the controllers in `src/main/webapp/js` (`app/`, `admin/`, `job/`, `common/`, with the libraries in `vendor/`).

  A rewrite of this frontend in React lives on the unmerged `react` branch, which replaces `src/main/webapp/js` wholesale and adds a `package.json`/`webpack.config.js` under `src/main/webapp`. Neither exists on `dev`, so an untracked `node_modules/`, `static/dist/`, or bundled `target/` left in the working tree is a leftover from that branch rather than part of this build.
- `provider/` — Service layer with the provider interfaces and implementations (geocode, district, address, geocache).
- `dao/` — Data access. `provider/` holds the external service DAOs — `HttpGoogleDao`, `HttpNYSGeoDao`, and `DistrictInfoDao` sit directly in it, with `shapefile/`, `streetfile/`, and `usps/` subpackages. Also `base/` (query-building and `SqlTable`, which is what reads `districts.type_info`), `model/` for entity DAOs, and `logger/`, `stats/`, `post_office/`.
- `model/` — Domain objects (address, geo, district, result, job, etc.).
- `service/` — Higher-level orchestration (address service, geocode service provider, district member provider, job processing, security/auth via Shiro).
- `client/` — API response and view objects for serialization.
- `scripts/` — Standalone scripts, notably `streetfinder/` for parsing Board of Election streetfile data.
- `util/`, `factory/`, `annotation/` — shared helpers, `SageThreadFactory`, and the test category annotations. Those annotations are in `src/main`, not `src/test`.
- `config/` — Spring/web/database/security configuration classes. `WebInitializer` bootstraps the servlet context programmatically (no web.xml needed at runtime).

**Jakarta namespace**: The project uses `jakarta.servlet`, `jakarta.annotation`, etc. (not the old `javax.*` equivalents). Shiro dependencies use the `jakarta` classifier.

**Database**: PostgreSQL. Schema-change SQL lives in `src/main/resources/sql/` and is applied manually — Flyway was removed, so do not create new `V{date}__`-prefixed versioned migration files (the existing ones are historical).

**District geometry**: `bin/admin/update_district_geometry.sh [SOURCE] DISTRICT_TYPE` loads the `districts.*` geometry tables. `SOURCE` is a zip archive (a local path, or a URL the script downloads first — GDAL can read a remote archive in place, but LATFOR's server 403s the range requests that takes), the URL of an ArcGIS REST feature service layer (read through GDAL's ESRIJSON driver, which pages the layer's `/query` endpoint), or any other URL GDAL can open directly — that last case covers the Socrata GeoJSON export the electric utility layer comes from. Most of our boundaries come from the NYS Civil Boundaries service (`.../NYS_Civil_Boundaries/FeatureServer`, layer 2 counties, 6 cities/towns, 7 villages) and election districts from NYS Elections Districts and Polling Locations (layer 4), and the senate, assembly, and congressional lines from the LATFOR plan shapefiles (`https://latfor.state.ny.us/maps/`), so there is no need to download anything by hand.

`bin/admin/sources.conf` holds the per-type defaults: `SENATE`, `ASSEMBLY`, `CONGRESSIONAL`, `COUNTY`, `TOWN_CITY`, `VILLAGE`, `ELECTION`, `CITY_COUNCIL`, and `ELECTRIC_UTILITY` have entries there, so `update_district_geometry.sh TOWN_CITY` takes no source argument and asks nothing — its source and its columns both come from the file. A configured type *only* loads from its entry — passing a `SOURCE` for one is an error, so redirecting it or changing a column means editing `sources.conf`. Conversely, a type with no entry has to be given a `SOURCE`, is prompted for its id column (`ID_COLUMN`), and keeps nothing else: every other column, the name included, is read from `sources.conf` alone and is never prompted for, so a type that needs one has to be listed there.

The load is an `ogr2ogr -overwrite`, which drops any column the source lacks, so `bin/admin/post_load/<type>.sql` runs afterwards to re-add the SAGE-specific ones and to correct the source data. Only that one file per type is run; anything else in `post_load/` is pulled in by an `\ir` from it, so a hook that grew large is split rather than added to the directory as a second entry point. What the current hooks do:

- `county.sql` — `code`, the first two digits of the SWIS code (a display code alongside `gnis_id`, so the hook enforces its own `NOT NULL`), plus (via `\ir add_county_links.sql`) `link`, the county health department page SAGE links to, from a name-keyed mapping checked into that file.
- `town_city.sql` — `code`, a ≤6-character abbreviation generated from the name by rule (initials for a `North`/`South`/`East`/`West` or `Mount`/`Fort` prefix, space removal after `De`/`La`/`Le`), with the collisions those rules produce fixed up from two lists of exceptions checked into the file. Also `voterfile_code`, the codes some counties use for municipalities in their voter files — only the counties that send them are listed, so most rows have none.
- `election.sql` — the source's `Election_District` reads like `Conklin 4`, qualified by municipality but not by county, so the hook (with `\ir election_parser.sql` and `\ir election_muni_cleanup.sql`) parses it, resolves the municipality to a `town_city_id`/`village_id`, and builds `id` and `name` by `concat_ws` over town/city, assembly district, village, ward, county legislature, and ED number — whichever of those the district actually uses. It ends with two checks that should both return nothing: one id built from two different field sets, and one name covering two ids.
- `electric_utility.sql` — `id`, since the source's `comp_id` files every municipally-run utility under 9999; those are qualified by the GNIS code of the municipality they serve. It also renames the source's `comp_full` to `name` up front.
- `village.sql` — builds no columns; it only fixes the source's data, expanding `St ` to `St. ` in the name/county/town and zeroing the placeholder `gnis_id`/`fips_code` on a village too new to have been assigned real ones.

A type whose identifying column is hook-built has no such column in the source, so its `sources.conf` `ID_COLUMN` names the column the hook will create and `EXTRA_COLUMNS` keeps whatever the hook keys on (`COUNTY`/`MUNICIPALITY`/`ELECTION_DISTRICT`, `comp_id`). `ID_COLUMN` is deliberately not called `CODE_COLUMN`: a `code` column is a separate, always hook-built display abbreviation that several types carry *alongside* their id — `county.code` from the SWIS code and `town_city.code` from the name, both of which sit next to a `gnis_id` that is what the districts are actually keyed on.

After the hook, the script enforces `NOT NULL` on the id column and on `name` when the table has one — which doubles as the check on the hook's work, since a district it found no id for fails there rather than in Java. A hook-built `code` is outside that gate, so the hooks that build one enforce its `NOT NULL` themselves. It then upserts the type into `districts.type_info` (`type_name` → `id_column`, the table SAGE reads to know which column identifies a district) and finally calls the `/admin/api/cleanMaps` endpoint, which needs the server to be up: the script pings `baseUrl` before doing anything else and, if it is not responding, warns and carries on with the load, skipping the `/cleanMaps` call at the end and printing the command to run by hand once the server is back.

**District identity**: a district's `DistrictId` (`model/district/DistrictId`, a record wrapping a string of digit groups joined by hyphens) is unique within its `DistrictType` and is what SAGE keys geometry and metadata on. It is deliberately separate from the code that appears in API responses — the hyphenated form exists because some types have no single natural identifier: an election district's id is its municipality, assembly district, village, ward, county legislative district, and ED number joined together, since the ED number alone repeats across the state.

`DistrictInfoDao` builds the map from ids to `DistrictInfo` for a type: for a type registered in `districts.type_info` it reads the district table and keeps every column except `gid` and `geom`, and for one that only comes from streetfiles it reads the distinct codes out of the streetfile table instead. `DistrictInfo` is just a column-name → value map, and its `name` key always exists — the DAO synthesizes one (`"<name> County"`, `"Village of <name>"`, `"Ward 3"`, `"<Type> District 12"`) when the table has no name column or the type has no names at all. `DistrictInfoCache` (over `DistrictIdCache`/`ImmutableCache`) holds this for every type, so lookups of a district's name or attributes go through the cache rather than a per-type DAO — this is why there is no longer a `CountyDao`.

**Security**: Apache Shiro for authentication. API requests are filtered by `controller/api/filter/ApiFilter` — requests matching `user.ip.filter` bypass API key checks; others require a valid key.

**Batch jobs**: Uploaded CSV files are geocoded/district-assigned in batch. Processing is scheduled via cron and uses multi-threaded execution.
