.. Sage  documentation master file, created by
   sphinx-quickstart on Fri Apr  5 15:15:48 2013.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

SAGE ADMIN API v2 Reference
=================================

The SAGE API exposes methods for geocoding, address correction, and district assignment given addresses or geo-coordinates
as input. The API supports ``JSON``, ``JSON-P``, and ``XML`` output formats.

Basics
~~~~~~

The API requests must be crafted to match the following structure::

    /admin/<group>/<method>?params..

All API requests need to be validated using an assigned API key however it is not required when requests are made from
within the NY Senate's network. The key is supplied using the query parameter ``key``::

    /admin/<group>/<method>?params..>&key=YOUR KEY HERE

The default output format is ``JSON``. To change the output to ``XML`` or ``JSON-P`` simply set the ``format`` query
parameter to ``xml`` or ``jsonp``. For example to set to xml::

    /admin/<group>/<method>?params..&format=xml

To output JSON-P a callback must also be specified.::

    /admin/<group>/<method>?params..&format=jsonp&callback=methodName

Groups
~~~~~~

Each API method belongs in a logical group in order to make it easy to understand the goal of the request. The supported
types for the ``group`` segment are as follows:

+-------------+----------------------------------------+
| Group       | Description                            |
+=============+========================================+
| api_        | General api calls used by the UI       |
+-------------+----------------------------------------+
| datadel_    | Deletes bad data in the DB             |
+-------------+----------------------------------------+
| datagen_    | Generates data to be used by SAGE      |
+-------------+----------------------------------------+
| regeocache_ | Updates the Geocache                   |
+-------------+----------------------------------------+

Methods
~~~~~~~

.. _common-query-parameters:

Common Query Parameters
-----------------------

The only true common params are the Authentication params. You must have admin Authentication & permissions to execute
these api calls.

Api
-------

The following methods are implemented for the api_ service:

+--------------------+---------------------------------------------+
| Method             | Description                                 |
+====================+=============================================+
| currentApiUsers    | Returns the current api users               |
+--------------------+---------------------------------------------+
| currentJobUsers    | Returns the current job users               |
+--------------------+---------------------------------------------+
| apiUserUsage       | Returns api user request stats              |
+--------------------+---------------------------------------------+
| usage              | Returns api user usage request stats        |
+--------------------+---------------------------------------------+
| geocodeUsage       | Returns geocode usage stats                 |
+--------------------+---------------------------------------------+
| jobStatuses        | Returns current job statuses                |
+--------------------+---------------------------------------------+
| deployment         | Returns deployment stats                    |
+--------------------+---------------------------------------------+
| exception          | Returns exception stats & info              |
+--------------------+---------------------------------------------+
| createApiUser      | Creates an api user                         |
+--------------------+---------------------------------------------+
| deleteApiUser      | Deletes an api user                         |
+--------------------+---------------------------------------------+
| createJobUser      | Creates a job user                          |
+--------------------+---------------------------------------------+
| deleteJobUser      | Deletes an job user                         |
+--------------------+---------------------------------------------+
| hideException      | Hides an exception from the admin UI        |
+--------------------+---------------------------------------------+

All of these api calls were made to be used with the Admin UI. They can be seen by clicking the various admin tabs

Datadel
-------

The following methods are implemented for the datadel_ service:

+---------------+------------------------------------------------+
| Method        | Description                                    |
+===============+================================================+
| zips/{offset} | Delete bad zips in the geocache from an offset |
+---------------+------------------------------------------------+
| states        | Delete bad zips from the geocache              |
+---------------+------------------------------------------------+

You must authenticate in one of the following ways an Api Key or username and password:

+-------------+-------------------------------------------------------------------------------+
| Param       | Description                                                                   |
+=============+===============================================================================+
| apiKey      | A key to access admin api                                                     |
+-------------+-------------------------------------------------------------------------------+
| username    | Admin username that you used to log into the admin panel                      |
+-------------+-------------------------------------------------------------------------------+
| password    | Admin password that you used to log into the admin panel                      |
+-------------+-------------------------------------------------------------------------------+


Zips
^^^^

The usage of zips api call::

    /admin/datadel/zips/0

The zips successful response::

    {
        success: true,
        message: "0: Success."
    }


States
^^^^^^

The usage of stats api call::

    /admin/datadel/states

The states successful response::

    {
        success: true,
        message: "0: Success."
    }


Refer to :ref:`common-query-parameters` to ensure that the correct fields are being used.

Datagen
-------

The datagen_ service has the following method(s).

+-------------+-----------------------------------------------------------------+
| Method      | Description                                                     |
+=======================+=======================================================+
| genmetadata/{option}  | Generate meta data on Representatives of NYS          |
+-----------------------+-------------------------------------------------------+
| countycodes           | Create county code file for Street file parsing       |
+-----------------------+-------------------------------------------------------+
| towncodes             | Create town code file for Street file parsing         |
+-----------------------+-------------------------------------------------------+

``genmetadata`` has the following optional parameters:

+------------------+-------------------------------------------------------------------------------+
| Param            | Description                                                                   |
+==================+===============================================================================+
| all              | Generate Meta data on Senate, Assembly, and Congressional members             |
+------------------+-------------------------------------------------------------------------------+
| senate           | Generate Meta data on Senate members                                          |
+------------------+-------------------------------------------------------------------------------+
| assembly         | Generate Meta data on Assembly members                                        |
+------------------+-------------------------------------------------------------------------------+
| congress         | Generate Meta data on Congressional members                                   |
+------------------+-------------------------------------------------------------------------------+
| s                | Generate Meta data on Senate members                                          |
+------------------+-------------------------------------------------------------------------------+
| a                | Generate Meta data on Assembly members                                        |
+------------------+-------------------------------------------------------------------------------+
| c                | Generate Meta data on Congressional members                                   |
+------------------+-------------------------------------------------------------------------------+

genmetadata
^^^^^^^^^^^

The usage of generate meta data api call::

    /admin/datagen/genmetadata/all

The Generate Meta Data successful response::

    {
        success: true,
        message: "0: Success."
    }

countycodes
^^^^^^^^^^^

The usage of generate meta data api call::

    /admin/datagen/countycodes

The Generate Meta Data successful response::

    {
        success: true,
        message: "0: Success."
    }


towncodes
^^^^^^^^^

The usage of generate meta data api call::

    /admin/datagen/countycodes

The Generate Meta Data successful response::

    {
        success: true,
        message: "0: Success."
    }


Regeocache
----------

The regeocache_ service has the following method(s):

+-------------+-----------------------------------------------------------------+
| Method      | Description                                                     |
+=============+=================================================================+
| zip                       | Regeocache zip codes we have geometry for         |
+-------------------------------------------------------------------------------+
| nysrefresh/{offset}      | Refresh the geocache with NYSGEO data              |
+-------------+-----------------------------------------------------------------+
| nysrefresh/dups/{offset} | Refresh just the dups with the NYSGEO web service  |
+-------------+-----------------------------------------------------------------+

A zip request is of the following form::

    /admin/regeocache/zip

The zip regeocache successful response::

    {
        success: true,
        message: "0: Success."
    }

A /nysrefresh/{offset} request is of the following form::

    /admin/regeocache/nysrefresh/0

The /nysrefresh/{offset} regeocache successful response::

    {
        success: true,
        message: "0: Success."
    }

A /nysrefresh/dups/{offset} request is of the following form::

    /admin/regeocache/nysrefresh/dups/0

The /nysrefresh/dups/{offset} regeocache successful response::

    {
        success: true,
        message: "Duplicate addresses Geocded Queries to NYS GEO: 74 Updated Records: 1047 Bad Records (Not found by USPS but rooftop level): 74 NYS Geo duplicate address records: 0"
    }

    Note: The numbers can change drastically depending on the offset and data that nysgeo corrects

Status Codes
~~~~~~~~~~~~

The following table lists all status codes. A positive response will usually have a status code of 0 while the rest are
generally error statuses:

+----------------------------------+------+---------------------------------------------------------------------------------+
| Status                           | Code | Description                                                                     |
+==================================+======+=================================================================================+
| SUCCESS                          | 0    | Success                                                                         |
+----------------------------------+------+---------------------------------------------------------------------------------+
| SERVICE_NOT_SUPPORTED            | 1    | The requested service is unsupported                                            |
+----------------------------------+------+---------------------------------------------------------------------------------+
| FEATURE_NOT_SUPPORTED            | 2    | The requested feature is unsupported                                            |
+----------------------------------+------+---------------------------------------------------------------------------------+
| PROVIDER_NOT_SUPPORTED           | 3    | The requested provider is unsupported                                           |
+----------------------------------+------+---------------------------------------------------------------------------------+
| ADDRESS_PROVIDER_NOT_SUPPORTED   | 4    | The requested address provider is unsupported                                   |
+----------------------------------+------+---------------------------------------------------------------------------------+
| GEOCODE_PROVIDER_NOT_SUPPORTED   | 5    | The requested geocoding provider is unsupported                                 |
+----------------------------------+------+---------------------------------------------------------------------------------+
| DISTRICT_PROVIDER_NOT_SUPPORTED  | 6    | The requested district assignment provider is unsupported                       |
+----------------------------------+------+---------------------------------------------------------------------------------+
| API_KEY_INVALID                  | 10   | The supplied API key could not be authenticated                                 |
+----------------------------------+------+---------------------------------------------------------------------------------+
| API_KEY_MISSING                  | 11   | An API key is required                                                          |
+----------------------------------+------+---------------------------------------------------------------------------------+
| API_REQUEST_INVALID              | 20   | The request is not in a valid format. Check the documentation for proper usage  |
+----------------------------------+------+---------------------------------------------------------------------------------+
| API_INPUT_FORMAT_UNSUPPORTED     | 21   | The requested input format is currently not supported                           |
+----------------------------------+------+---------------------------------------------------------------------------------+
| API_OUTPUT_FORMAT_UNSUPPORTED    | 22   | The requested output format is currently not supported                          |
+----------------------------------+------+---------------------------------------------------------------------------------+
| JSONP_CALLBACK_NOT_SPECIFIED     | 23   | A callback signature must be specified as a parameter e.g &callback=method")    |
+----------------------------------+------+---------------------------------------------------------------------------------+
| RESPONSE_MISSING_ERROR           | 30   | No response from service provider                                               |
+----------------------------------+------+---------------------------------------------------------------------------------+
| RESPONSE_PARSE_ERROR             | 31   | Error parsing response from service provider                                    |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_INPUT_PARAMS             | 40   | One or more parameters are missing                                              |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_ADDRESS                  | 41   | An address is required                                                          |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_GEOCODE                  | 42   | A valid geocoded coordinate pair is required                                    |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_ZIPCODE                  | 43   | A zipcode is required                                                           |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_STATE                    | 44   | A state is required                                                             |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_POINT                    | 45   | A coordinate pair is required                                                   |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_GEOCODED_ADDRESS         | 46   | A valid geocoded address is required                                            |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INVALID_INPUT_PARAMS             | 50   | One or more parameters are invalid                                              |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INVALID_ADDRESS                  | 51   | The supplied address is invalid                                                 |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INVALID_GEOCODE                  | 52   | The supplied geocoded coordinate pair is invalid                                |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INVALID_ZIPCODE                  | 53   | The supplied zipcode is invalid                                                 |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INVALID_STATE                    | 54   | The supplied state is invalid or is not supported                               |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INVALID_BATCH_ADDRESSES          | 55   | The supplied batch address list could not be parsed                             |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INVALID_BATCH_POINTS             | 56   | The supplied batch point list could not be parsed                               |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NON_NY_STATE                     | 57   | The address you have supplied is not a valid New York address.                  |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INSUFFICIENT_INPUT_PARAMS        | 60   | One or more parameters are insufficient                                         |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INSUFFICIENT_ADDRESS             | 61   | The supplied address is missing one or more parameters                          |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INSUFFICIENT_GEOCODE             | 62   | The supplied geocoded is missing one or more parameters                         |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NO_DISTRICT_RESULT               | 70   | District assignment returned no results                                         |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NO_GEOCODE_RESULT                | 71   | Geocode service returned no results                                             |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NO_REVERSE_GEOCODE_RESULT        | 72   | Reverse Geocode service returned no results                                     |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NO_ADDRESS_VALIDATE_RESULT       | 73   | The address could not be validated                                              |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NO_STREET_LOOKUP_RESULT          | 74   | Street lookup returned no results for the given zip5                            |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NO_MAP_RESULT                    | 80   | Map request returned no results                                                 |
+----------------------------------+------+---------------------------------------------------------------------------------+
| UNSUPPORTED_DISTRICT_MAP         | 81   | Maps for the requested district type are not available                          |
+----------------------------------+------+---------------------------------------------------------------------------------+
| MISSING_DISTRICT_CODE            | 82   | A district code is required                                                     |
+----------------------------------+------+---------------------------------------------------------------------------------+
| NOT_FOUND                        | 404  | Not Found                                                                       |
+----------------------------------+------+---------------------------------------------------------------------------------+
| INTERNAL_ERROR                   | 500  | Internal Server Error                                                           |
+----------------------------------+------+---------------------------------------------------------------------------------+
| DATABASE_ERROR                   | 501  | Database Error                                                                  |
+----------------------------------+------+---------------------------------------------------------------------------------+
| RESPONSE_ERROR                   | 502  | Application failed to provide a response                                        |
+----------------------------------+------+---------------------------------------------------------------------------------+
| RESPONSE_SERIALIZATION_ERROR     | 503  | Failed to serialize response                                                    |
+----------------------------------+------+---------------------------------------------------------------------------------+

.. toctree::
   :maxdepth: 2
