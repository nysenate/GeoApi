.. Sage  documentation master file, created by
   sphinx-quickstart on Fri Apr  5 15:15:48 2013.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

SAGE ADMIN API v2 Reference
=================================

The SAGE Admin API exposes methods for administering the SAGE service: managing API and job
users, viewing usage and deployment statistics, regenerating SAGE's underlying data sets, and
re-caching district map geometry. These endpoints back the SAGE Admin UI but can also be called
directly. Responses are returned as ``JSON``.

Basics
~~~~~~

Admin API requests are crafted to match the following structure::

    /admin/api/<method>?params..

A few data-generation methods live under a ``datagen`` sub-path::

    /admin/api/datagen/<method>?params..

Authentication
~~~~~~~~~~~~~~~

Every admin method requires admin-level authentication. A request is authorized if any of the
following holds:

+-------------+------------------------------------------------------------------------+
| Param       | Description                                                            |
+=============+========================================================================+
| (session)   | The caller already has an authenticated ADMIN session (for example,    |
|             | after signing in through /admin/login).                                |
+-------------+------------------------------------------------------------------------+
| username    | Admin username, supplied together with password.                       |
+-------------+------------------------------------------------------------------------+
| password    | Admin password, supplied together with username.                       |
+-------------+------------------------------------------------------------------------+
| key         | An API key belonging to a user that has admin permissions.             |
+-------------+------------------------------------------------------------------------+

For example, authenticating with an admin API key::

    /admin/api/currentApiUsers?key=YOUR_KEY_HERE

Requests that fail authentication receive a response indicating the credentials were invalid.

Session Methods
~~~~~~~~~~~~~~~

The following methods manage the admin session. They are served directly under ``/admin``:

+----------+--------+---------------------------------------------------------+
| Method   | HTTP   | Description                                             |
+==========+========+=========================================================+
| login    | POST   | Log in to the SAGE admin panel with admin credentials   |
+----------+--------+---------------------------------------------------------+
| logout   | GET    | Log out of the SAGE admin panel                         |
+----------+--------+---------------------------------------------------------+

``login`` requires ``username`` and ``password`` query parameters. On success the caller is
redirected to the admin home page; on failure the login page is redisplayed with an error.

::

    /admin/login        (POST, params: username, password)
    /admin/logout       (GET)

Api
-------

The following methods are served under ``/admin/api`` and back the various admin UI tabs:

+------------------+--------+----------------------------------------------------------------+
| Method           | HTTP   | Description                                                    |
+==================+========+================================================================+
| currentApiUsers  | GET    | Returns the current API users                                  |
+------------------+--------+----------------------------------------------------------------+
| currentJobUsers  | GET    | Returns the current job users                                  |
+------------------+--------+----------------------------------------------------------------+
| apiUserUsage     | GET    | Returns API user request counts                                |
+------------------+--------+----------------------------------------------------------------+
| usage            | GET    | Returns API usage request stats                                |
+------------------+--------+----------------------------------------------------------------+
| geocodeUsage     | GET    | Returns geocode usage stats                                    |
+------------------+--------+----------------------------------------------------------------+
| deployment       | GET    | Returns deployment stats                                       |
+------------------+--------+----------------------------------------------------------------+
| createApiUser    | POST   | Creates an API user                                            |
+------------------+--------+----------------------------------------------------------------+
| deleteApiUser    | POST   | Deletes an API user                                            |
+------------------+--------+----------------------------------------------------------------+
| createJobUser    | POST   | Creates a job user                                             |
+------------------+--------+----------------------------------------------------------------+
| deleteJobUser    | POST   | Deletes a job user                                             |
+------------------+--------+----------------------------------------------------------------+
| cleanMaps        | GET    | Validates/cleans cached map geometry for a district type       |
+------------------+--------+----------------------------------------------------------------+
| recache          | GET    | Rebuilds the cached district map geometry from the shapefiles  |
+------------------+--------+----------------------------------------------------------------+

The ``usage``, ``apiUserUsage``, and ``geocodeUsage`` stats methods accept optional ``from`` and
``to`` timestamps to bound the reporting window.

createApiUser
^^^^^^^^^^^^^

Creates a new API user. Required and optional parameters:

+------------+----------+----------------------------------------------------+
| Param      | Required | Description                                        |
+============+==========+====================================================+
| name       | yes      | Unique name for the API user                       |
+------------+----------+----------------------------------------------------+
| desc       | no       | Free-text description of the API user              |
+------------+----------+----------------------------------------------------+
| admin      | no       | true to grant the user admin permissions           |
+------------+----------+----------------------------------------------------+

The usage of the createApiUser call::

    /admin/api/createApiUser        (POST, params: name, desc, admin)

A successful response::

    {
        success: true,
        message: "Added new API User with id 5"
    }

deleteApiUser
^^^^^^^^^^^^^

Deletes an API user by id.

+------------+----------+----------------------------------------------------+
| Param      | Required | Description                                        |
+============+==========+====================================================+
| id         | yes      | Id of the API user to delete                       |
+------------+----------+----------------------------------------------------+

::

    /admin/api/deleteApiUser        (POST, params: id)

createJobUser
^^^^^^^^^^^^^

Creates a new batch-job user.

+-------------+----------+----------------------------------------------------+
| Param       | Required | Description                                        |
+=============+==========+====================================================+
| email       | yes      | Email/login for the job user                       |
+-------------+----------+----------------------------------------------------+
| password    | yes      | Password for the job user                          |
+-------------+----------+----------------------------------------------------+
| firstname   | no       | First name                                         |
+-------------+----------+----------------------------------------------------+
| lastname    | no       | Last name                                          |
+-------------+----------+----------------------------------------------------+
| admin       | no       | true to grant the user admin permissions           |
+-------------+----------+----------------------------------------------------+

::

    /admin/api/createJobUser        (POST, params: email, password, firstname, lastname, admin)

deleteJobUser
^^^^^^^^^^^^^

Deletes a job user by id.

+------------+----------+----------------------------------------------------+
| Param      | Required | Description                                        |
+============+==========+====================================================+
| id         | yes      | Id of the job user to delete                       |
+------------+----------+----------------------------------------------------+

::

    /admin/api/deleteJobUser        (POST, params: id)

cleanMaps
^^^^^^^^^

Validates and cleans the cached map geometry for a single district type.

+------------+----------+--------------------------------------------------------------+
| Param      | Required | Description                                                  |
+============+==========+==============================================================+
| type       | yes      | District type (e.g. senate, assembly, congressional)         |
+------------+----------+--------------------------------------------------------------+

::

    /admin/api/cleanMaps?type=senate

A successful response::

    {
        success: true,
        message: "Cleaned maps"
    }

If the geometry table for the type is empty an error is returned; if some geometries are invalid
the call still succeeds but reports that manual fixes are required.

recache
^^^^^^^

Rebuilds the cached district map geometry from the LATFOR shapefiles. This is the same operation
performed when district maps are refreshed.

::

    /admin/api/recache

A successful response::

    {
        success: true,
        message: "0: Success."
    }

Datagen
-------

The ``datagen`` service regenerates data sets that SAGE depends on. Its methods are served under
``/admin/api/datagen``:

+------------------------+--------+------------------------------------------------------------+
| Method                 | HTTP   | Description                                                |
+========================+========+============================================================+
| streetfile             | GET    | Regenerates the consolidated streetfile and reloads it     |
+------------------------+--------+------------------------------------------------------------+
| genmetadata/{option}   | GET    | Generates metadata on NYS Senate/Assembly/Congress members |
+------------------------+--------+------------------------------------------------------------+
| post-offices           | GET    | Refreshes the post office data set                         |
+------------------------+--------+------------------------------------------------------------+

streetfile
^^^^^^^^^^

Re-parses the source streetfiles, resolves conflicts, and replaces the streetfile data in the
database.

+-------------+----------+--------------------------------------------------------------+
| Param       | Default  | Description                                                  |
+=============+==========+==============================================================+
| voterFirst  | false    | When true, voter streetfiles take priority over county files |
|             |          | during conflict resolution; otherwise county files take      |
|             |          | priority.                                                    |
+-------------+----------+--------------------------------------------------------------+
| threshold   | 0.8      | Confidence threshold used when resolving conflicting         |
|             |          | records.                                                     |
+-------------+----------+--------------------------------------------------------------+

::

    /admin/api/datagen/streetfile?voterFirst=false&threshold=0.8

A successful response::

    {
        success: true,
        message: "0: Success."
    }

If there are no streetfiles staged for processing, the response reports that no action was taken.

genmetadata
^^^^^^^^^^^

Generates metadata about NYS representatives. The ``{option}`` path segment selects which
chambers to (re)generate:

+------------+-------------------------------------------------------------------+
| Option     | Description                                                       |
+============+===================================================================+
| all        | Generate metadata for Senate, Assembly, and Congress members      |
+------------+-------------------------------------------------------------------+
| senate     | Generate metadata for Senate members (alias: s)                   |
+------------+-------------------------------------------------------------------+
| assembly   | Generate metadata for Assembly members (alias: a)                 |
+------------+-------------------------------------------------------------------+
| congress   | Generate metadata for Congressional members (alias: c)            |
+------------+-------------------------------------------------------------------+

The usage of the generate metadata call::

    /admin/api/datagen/genmetadata/all

A successful response::

    {
        success: true,
        message: "0: Success."
    }

post-offices
^^^^^^^^^^^^

Refreshes SAGE's post office data set.

::

    /admin/api/datagen/post-offices

A successful response::

    {
        success: true,
        message: "0: Success."
    }

On failure a ``POST_OFFICE_REFRESH_FAILURE`` (code 48) error is returned.

Status Codes
~~~~~~~~~~~~

The following table lists the status codes returned by SAGE. A successful response has a status
code of ``0``; the rest are error statuses:

+-------------------------------+------+----------------------------------------------------------------------------------------+
| Status                        | Code | Description                                                                            |
+===============================+======+========================================================================================+
| SUCCESS                       | 0    | Success.                                                                               |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| EMPTY_GEOMETRY_TABLE          | 1    | The new geometry table is empty.                                                       |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| PROVIDER_NOT_SUPPORTED        | 3    | The requested provider is unsupported.                                                 |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| API_KEY_INVALID               | 10   | The supplied API key could not be authenticated.                                       |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| API_KEY_MISSING               | 11   | An API key is required.                                                                |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| API_REQUEST_INVALID           | 20   | The request is not in a valid format. Check the documentation for proper usage.        |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| RESPONSE_PARSE_ERROR          | 31   | Error parsing response from service provider.                                          |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| INVALID_ADDRESS               | 41   | A valid address is required.                                                           |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| MISSING_POINT                 | 45   | A coordinate pair is required.                                                         |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| MISSING_GEOCODED_ADDRESS      | 46   | The address could not be matched by the geocoder; verify it or try a different one.    |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| POST_OFFICE_REFRESH_FAILURE   | 48   | Failed to refresh post office data.                                                    |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| INVALID_GEOCODE               | 52   | The geocoding process did not yield a successful response.                             |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| MISSING_GEOCODER              | 53   | No geocoder provided.                                                                  |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| INVALID_BATCH_ADDRESSES       | 55   | The supplied batch address list could not be parsed.                                   |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| INVALID_BATCH_POINTS          | 56   | The supplied batch point list could not be parsed.                                     |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| NON_NY_STATE                  | 57   | The supplied address is not a valid New York address. Only NY addresses are supported. |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| INSUFFICIENT_ADDRESS          | 61   | The supplied address does not contain enough information. Add a city, state, or zip.   |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| INSUFFICIENT_GEOCODE          | 62   | The supplied geocode does not contain enough information to continue processing.       |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| NO_GEOCODE_RESULT             | 71   | Geocode service returned no results.                                                   |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| NO_REVERSE_GEOCODE_RESULT     | 72   | Reverse geocode service returned no results.                                           |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| NO_ADDRESS_VALIDATE_RESULT    | 73   | The address could not be validated.                                                    |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| NO_STREET_LOOKUP_RESULT       | 74   | Street lookup returned no results for the given zip5.                                  |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| NO_MAP_RESULT                 | 80   | Map request returned no results.                                                       |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| UNSUPPORTED_DISTRICT_MAP      | 81   | Maps for the requested district type are not available.                                |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| MISSING_DISTRICT_CODE         | 82   | A district code is required.                                                           |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| BAD_OVERLAY                   | 83   | Same type overlay is not supported.                                                    |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| NO_STREETFILES_TO_PROCESS     | 90   | There were no streetfiles to process. No action was taken.                             |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| GENERAL_FAILURE               | 100  | API call did not succeed.                                                              |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| INTERNAL_ERROR                | 500  | Internal Server Error.                                                                 |
+-------------------------------+------+----------------------------------------------------------------------------------------+
| RESPONSE_ERROR                | 502  | Application failed to provide a response.                                              |
+-------------------------------+------+----------------------------------------------------------------------------------------+

.. toctree::
   :maxdepth: 2
