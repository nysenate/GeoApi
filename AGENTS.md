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

**Package layout**: the top-level packages under `gov.nysenate.sage` (`controller/`, `provider/`, `dao/`, `model/`, `service/`, `client/`, `scripts/`, `config/`, `util/`, `factory/`, `annotation/`) hold what their names say. The parts that a reading of the tree would get wrong:

- The frontend is AngularJS rather than server-rendered. `AngularAppCtrl` only picks which JSP in `src/main/webapp/WEB-INF/views` to return; the pages themselves are Angular templates (`ng-*` attributes, `{{ }}` bindings) inline in the JSP, driven by the controllers in `src/main/webapp/js`.
- A React rewrite of that frontend lives on the unmerged `react` branch, which replaces `src/main/webapp/js` wholesale and adds a `package.json`/`webpack.config.js` under `src/main/webapp`. Neither exists on `dev`, so an untracked `node_modules/`, `static/dist/`, or bundled `target/` in the working tree is a leftover from that branch rather than part of this build.
- `dao/provider/` holds the external service DAOs, but `HttpGoogleDao`, `HttpNYSGeoDao`, and `DistrictInfoDao` sit directly in it rather than in its `shapefile/`, `streetfile/`, `usps/` subpackages. `SqlTable` in `dao/base/` is what reads `districts.type_info`.
- The `@UnitTest`/`@IntegrationTest` annotations are in `src/main`, not `src/test`.
- `WebInitializer` bootstraps the servlet context programmatically, so there is no `web.xml` at runtime.

**Jakarta namespace**: The project uses `jakarta.servlet`, `jakarta.annotation`, etc. (not the old `javax.*` equivalents). Shiro dependencies use the `jakarta` classifier.

**Database**: PostgreSQL. Schema-change SQL lives in `src/main/resources/sql/` and is applied manually — Flyway was removed, so do not create new `V{date}__`-prefixed versioned migration files (the existing ones are historical).

**District geometry**: `bin/admin/update_district_geometry.sh [SOURCE] DISTRICT_TYPE` loads the `districts.*` geometry tables with an `ogr2ogr -overwrite`, from a shapefile archive or a feature service layer. Two files govern a load, and both are worth reading before changing one:

- `bin/admin/sources.conf` — the source and kept columns for each type that comes from a known dataset, with the rules in its header. Most types are listed and load without a `SOURCE` argument.
- `bin/admin/post_load/<type>.sql` — the per-type hook that re-adds the SAGE-specific columns the overwrite drops and corrects the source data. Only this one file per type is run; anything else in `post_load/` is pulled in by an `\ir` from it.

The script then upserts the type into `districts.type_info` (`type_name` → `id_column`, the table SAGE reads to know which column identifies a district) and calls `/admin/api/cleanMaps`, which needs the server to be up.

**District identity**: a district's `DistrictId` (`model/district/DistrictId`, a record wrapping a string of digit groups joined by hyphens) is unique within its `DistrictType` and is what SAGE keys geometry and metadata on. It is deliberately separate from the code that appears in API responses — the hyphenated form exists because some types have no single natural identifier: an election district's id is its municipality, assembly district, village, ward, county legislative district, and ED number joined together, since the ED number alone repeats across the state.

`DistrictInfoDao` builds the id → `DistrictInfo` map for a type, from the district table for a type registered in `districts.type_info` and from the streetfile table for one that only comes from streetfiles. `DistrictInfo` is a column-name → value map whose `name` key always exists, synthesized when the source has no name of its own. `DistrictInfoCache` holds this for every type, so lookups of a district's name or attributes go through the cache rather than a per-type DAO.

**Security**: Apache Shiro for authentication. API requests are filtered by `controller/api/filter/ApiFilter` — requests matching `user.ip.filter` bypass API key checks; others require a valid key.

**Batch jobs**: Uploaded CSV files are geocoded/district-assigned in batch. Processing is scheduled via cron and uses multi-threaded execution.
