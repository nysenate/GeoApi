.. Sage  documentation master file, created by
   sphinx-quickstart on Fri Apr  5 15:15:48 2013.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

SAGE API v2 Reference
=================================

The SAGE API exposes methods for geocoding, address correction, and district assignment given addresses or geo-coordinates
as input. The API supports ``JSON``, ``JSON-P``, and ``XML`` output formats.

SAGE is also able to handle BluebirdCRM_ district assignment requests.

Basics
~~~~~~

The API requests must be crafted to match the following structure::

    /api/v2/<group>/<method>?params..

API requests need to be validated using an assigned API key, unless made from
within the NY Senate's network. The key is supplied using the query parameter ``key``::

    /api/v2/<group>/<method>?<params..>&key=YOUR KEY HERE

The default output format is ``JSON``. To change the output to ``XML`` or ``JSON-P`` simply set the ``format`` query
parameter to ``xml`` or ``jsonp``. For example to set to xml::

    /api/v2/geo/geocode?addr=200 State St, Albany NY&format=xml

To output JSON-P a callback must also be specified.::

    /api/v2/geo/geocode?addr=200 State St, Albany NY&format=jsonp&callback=methodName

Groups
~~~~~~

Each API method belongs in a logical group in order to make it easy to understand the goal of the request. The supported
types for the ``group`` segment are as follows:

+-------------+----------------------------------------+
| Group       | Description                            |
+=============+========================================+
| address_    | Address Lookup and Validation          |
+-------------+----------------------------------------+
| geo_        | Geocode and Reverse Geocode            |
+-------------+----------------------------------------+
| district_   | District Assignment                    |
+-------------+----------------------------------------+
| street_     | Street Lookup                          |
+-------------+----------------------------------------+
| map_        | Map Data                               |
+-------------+----------------------------------------+

Methods
~~~~~~~

.. _common-query-parameters:

Common Query Parameters
-----------------------

Many of the methods listed below follow a similar convention for inputting addresses or geo-coordinates.
To supply a single address string use the parameter ``addr``::

    /api/v2/<group>/<method>?addr=200 State St, Albany NY 12210

To supply the individual components use the following parameters: ``addr1``, ``addr2``, ``city``, ``state``, ``zip5``, ``zip4``. The above query would be written as::

    /api/v2/<group>/<method>?addr1=200 State St&city=Albany&state=NY&zip5=12210

Geo-coordinate pairs can be supplied to the appropriate method using ``lat`` and ``lon``::

    /api/v2/<group>/<method>?lat=43.00&lon=-73.10

Address
-------

The following methods are implemented for the address_ service:

+-------------+---------------------------------------------+
| Method      | Description                                 |
+=============+=============================================+
| validate    | Validate the given address                  |
+-------------+---------------------------------------------+
| citystate   | Lookup the city and state given the zipcode |
+-------------+---------------------------------------------+

The available providers are:

+-------------+---------------------------------------------+
| Provider    | Description                                 |
+=============+=============================================+
| AMS        | USPS AMS Address Correction                 |
+-------------+---------------------------------------------+
| AIS        | USPS AIS Address Correction                 |
+-------------+---------------------------------------------+

The usage of ``validate`` with an address input::

    /api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY

The validated response::

    {
      "status": "SUCCESS",
      "sources": "AMS",
      "address": {
        "addr1": "44 Fairlawn Ave",
        "addr2": "",
        "city": "Albany",
        "state": "NY",
        "zip5": "12203",
        "zip4": "1914"
      },
      "validated": true,
      "messages": [
        "Status: Default Match",
        "Missing Secondary Number - ZIP+4 information indicates this address is a building. The address as submitted does not contain an apartment/suite number.",
        "Address Standardized - The delivery address was standardized. For example, if STREET was in the delivery address, the system will return ST as its standard spelling. "
      ],
      "statusCode": 0,
      "description": "Success."
    }

A failed validation response::

    {
      "status" : "NO_ADDRESS_VALIDATE_RESULT",
      "source" : "AMS",
      "address" : null,
      "validated" : false,
      "statusCode" : 73,
      "description" : "The address could not be validated."
    }

.. caution:: USPS address validation requires addr1 and at least one of city or state explicitly specified in the query parameters.

The ``punct`` parameter can be supplied if abbreviations require a period appended to them. Simply add ``punct=true`` to the url
to enable punctuation.

The usage of ``citystate`` with a zip code input::

    /api/v2/address/citystate?zip5=12210

The city/state response::

    {
      "status" : "SUCCESS",
      "source" : "AMS",
      "city" : "ALBANY",
      "state" : "NY",
      "zip5" : "12210",
      "statusCode" : 0,
      "description" : "Success."
    }

A failed city/state response with invalid input::

    {
      "status": "NO_ADDRESS_VALIDATE_RESULT",
      "sources": "AMS",
      "state": null,
      "city": null,
      "zip5": null,
      "statusCode": 73,
      "description": "The address could not be validated."
    }

To force the request to use a certain provider supply the query parameter ``provider``::

    /api/v2/address/<method>?<params..>&provider=AMS
    /api/v2/address/<method>?<params..>&provider=AIS

Geo
---

The following methods are implemented for the geo_ service:

+-------------+---------------------------------------------+
| Method      | Description                                 |
+=============+=============================================+
| geocode     | Geocode the given address                   |
+-------------+---------------------------------------------+
| revgeocode  | Obtain address from given coordinate pair   |
+-------------+---------------------------------------------+

The available providers are:

+-------------+---------------------------------------------+
| Provider    | Description
+=============+=============================================+
| GEOCACHE    | Cached geocodes                             |
+-------------+---------------------------------------------+
| NYSGEO      | NYS geocoding service                       |
+-------------+---------------------------------------------+
| GOOGLE      | Paid geocoding service                      |
+-------------+---------------------------------------------+

Methods have the following optional parameters:

+-------------+---------------------------------------------+
| Param       | Description                                 |
+=============+=============================================+
| geocoder    | Specify which geocode provider to use       |
+-------------+---------------------------------------------+

For example to use just NYSGEO::

    /api/v2/geo/<method>?<params..>&provider=NYSGEO

Geocode
^^^^^^^

The usage of ``geocode`` with an address input::

    /api/v2/geo/geocode?addr=200 State St, Albany NY 12210
    /api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210

The geocode response::

    {
        status: "SUCCESS",
        source: "GEOCACHE",
        address: {
            addr1: "200 State Street",
            addr2: "",
            city: "Albany",
            state: "NY",
            zip5: "12210",
            zip4: ""
        },
        geocode: {
            lat: 42.6533668,
            lon: -73.7599828,
            quality: "HOUSE",
            method: "GOOGLE",
            cached: true,
            openLocCode: "87J8M63R+82"
        },
        geocoded: true,
        description: "Success.",
        statusCode: 0
    }

The ``source`` indicates where the response was returned from whereas ``geocode.method`` indicates where the geocode was computed.

The ``address`` is typically a normalized representation of the input address.

The ``geocode.quality`` metric indicates the accuracy/confidence level of the geocode. A successful geocode response will have
one of the following quality levels ordered from most accurate to least:

* POINT
* HOUSE
* ZIP_EXT
* STREET
* ZIP
* CITY

An unsuccessful response will resemble the following::

    {
      "status" : "NO_GEOCODE_RESULT",
      "source" : "GOOGLE",
      "address" : null,
      "geocode" : null,
      "geocoded" : false,
      "statusCode" : 71,
      "description" : "Geocode service returned no results."
    }

A ``RESPONSE_PARSE_ERROR`` status will also indicate a failed geocode operation.

Reverse Geocode
^^^^^^^^^^^^^^^

The usage of ``revgeocode`` with a coordinate pair input::

    /api/v2/geo/revgeocode?lat=42.652030&lon=-73.757590

The reverse geocode response::

    {
      "status": "SUCCESS",
      "sources": "NYSGEO",
      "address": {
        "addr1": "25 Eagle St",
        "addr2": "",
        "city": "Albany",
        "state": "NY",
        "zip5": "12207",
        "zip4": "1901",
      },
      "geocode": {
        "lat": "42.65240034765009",
        "lon": "-73.75694840989031",
        "quality": "UNKNOWN",
        "method": "NYSGEO",
        "cached": false,
        "openLocCode": "87J8M62V+X6"
      },
      "revGeocoded": true,
      "statusCode": 0,
      "description": "Success."
    }

It is identical to the geocode response except for the ``revGeocoded`` field that indicates whether the reverse geocoding succeeded.

.. _batch-geocode:

Batch Geocode
^^^^^^^^^^^^^

Multiple addresses can be geocoded with a single query using the batch api call.
The format of the batch geocoding call is::

    /api/v2/geo/geocode/batch

The format of the batch reverse geocoding call is::

    /api/v2/geo/revgeocode/batch

The addresses for geocoding and points for reverse geocoding must be JSON encoded and sent in the POST request payload.
The fields are identical to the query parameter fields for both address and point. Likewise any options can be specified
the same way as the single request version. For example to specify provider::

    /api/v2/geo/geocode/batch?provider=PROVIDER_NAME

A sample batch geocoding request in PHP::

    <?php
    $addresses = array(
        array(
            "addr1" => "100 Nyroy Dr",
            "city" => "Troy",
            "state" => "NY",
            "zip5" => "12180"
        ),
        array(
            "addr1" => "44 Fairlawn Ave",
            "city" => "Albany",
            "state" => "NY",
            "zip5" => "12203"
        )
    );

    $post_body = json_encode($addresses);

    $ch = curl_init("http://localhost:8080/GeoApi/geo/geocode/batch");
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post_body);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        'Content-Type: application/json',
        'Content-Length: ' . strlen($post_body)
    ));

    $result = curl_exec($ch);

And the response::

    {  "results" :[
        { "status" : "SUCCESS",
          "source" : "GeoCache", "messages" : [ ],
          "address" : { "addr1" : "100 Nyroy Dr", "addr2" : "", "city" : "Troy", "state" : "NY", "zip5" : "12180", "zip4" : "" },
          "geocode" : { "lat" : 42.741112, "lon" : -73.668762, "quality" : "HOUSE", "method" : "YahooDao" }, "geocoded" : true,
          "statusCode" : 0,
          "description" : "Success."
        },
        { "status" : "SUCCESS",
          "source" : "GeoCache",
          "messages" : [ ],
          "address" : { "addr1" : "44 Fairlawn Ave", "addr2" : "", "city" : "Albany", "state" : "NY", "zip5" : "12203", "zip4" : "" },
          "geocode" : { "lat" : 42.670583, "lon" : -73.799606, "quality" : "HOUSE", "method" : "YahooDao" },
          "geocoded" : true, "statusCode" : 0, "description" : "Success."
        }
      ],
      "total" : 2
    }

The results array consists of the same geocode responses that would be returned using the single geocode call.
The ``total`` field simply indicates the number of records returned. The order of the results should match the
order of the addresses in the JSON payload.

In the event that the payload fails to parse, the response will be::

    {
      "status" : "INVALID_BATCH_ADDRESSES",
      "source" : "GeocodeController",
      "messages" : [ ],
      "statusCode" : 55,
      "description" : "The supplied batch address list could not be parsed."
    }

Refer to :ref:`common-query-parameters` to ensure that the correct fields are being used.

District
--------

The district_ service has the following method(s).

+-------------+-----------------------------------------------------------------+
| Method      | Description                                                     |
+=============+=================================================================+
| assign      | Assign district information given an address or coordinate pair |
+-------------+-----------------------------------------------------------------+
| bluebird    | Performs district assign with preset options for Bluebird       |
+-------------+-----------------------------------------------------------------+

The available providers are:

+-------------+--------------------------------------+------------------+
| Provider    | Description                          | Requirements     |
+=============+======================================+==================+
| streetfile  | Street file database.                | Address          |
+-------------+--------------------------------------+------------------+
| shapefile   | In-database district shapes.         | Geocode          |
+-------------+--------------------------------------+------------------+

``assign`` has the following optional parameters:

+------------------+-------------------------------------------------------------------------------+
| Param            | Description                                                                   |
+==================+===============================================================================+
| districtSource   | Specify which district provider to use. Overrides 'districtStrategy'          |
+------------------+-------------------------------------------------------------------------------+
| geocoder         | Specify which geocode provider to use.                                        |
+------------------+-------------------------------------------------------------------------------+
| uspsValidate     | If true: USPS will be used to perform address correction.                     |
+------------------+-------------------------------------------------------------------------------+
| usePunct         | If true: punctuation will be added to abbreviations.                          |
+------------------+-------------------------------------------------------------------------------+
| showMaps         | If true: map data is appended for each district.                              |
+------------------+-------------------------------------------------------------------------------+

If the districtSource is not specified, the service will utilize multiple sources to provide the most accurate result.
If the geocoder is not specified, the service will iterate through a series of geocoders if needed until a geocode match is obtained.
Specifying districtSource or geocoder is not recommended as it may reduce the accuracy of the results.

.. caution:: USPS validation will only work when addr1, city/zip, and state are provided. See address section above for details.

Assign
^^^^^^

The default query usage is as follows::

    Unparsed Format:
    /api/v2/district/assign?addr=280 Madison Ave, New York, NY

    Parsed Format:
    /api/v2/district/assign?addr1=280 Madison Ave&city=New York&state=NY

The district assignment response::

    {
      "status" : "SUCCESS",
      "source" : "STREETFILE, SHAPEFILE",
      "address" : {
        "addr1" : "280 Madison Ave",
        "addr2" : "",
        "city" : "New York",
        "state" : "NY",
        "zip5" : "10016",
        "zip4" : "0801"
      },
      "geocode" : {
        "lat" : 40.7514214,
        "lon" : -73.9805145,
        "quality" : "HOUSE",
        "method" : "GOOGLE",
        "cached": true,
        "openLocCode": "87G8Q229+HQ"
      },
      "geocoded" : true,
      "districtAssigned" : true,
      "senateAssigned" : true,
      "uspsValidated" : true,
      "matchLevel" : "HOUSE",
      "districts" : {
        "senate" : {
          "name" : "NY Senate District 28",
          "district" : "28",
          "senator" : (excluded for length)
        },
        "congressional" : {
          "name" : "NY Congressional District 12",
          "district" : "12",
          "map": null,
          "member": {
            "name": "Nadler, Jerrold",
                "url": "https://nadler.house.gov"
            }
        },
        "assembly" : {
          "name" : "NY Assembly District 73",
          "district" : "73",
          "map": null,
          "member": {
            "name": "Alex Bores",
            "url": "https://www.nyassembly.gov/mem/Alex-Bores"
          }
        },
        "county" : {
          "name" : "New York County",
          "district" : "62",
          "map": null
        },
        "election" : {
          "name" : null,
          "district" : "8",
          "map": null
        },
        "school" : {
          "name" : "Manhattan SD",
          "district" : "369",
          "map": null
        },
        "town" : {
          "name" : "New York",
          "district" : "-NYC",
          "map": null
        },
        "zip" : {
          "name" : "Zipcode 10016",
          "district" : "10016",
          "map": null
        },
        "cleg" : null,
        "ward" : null,
        "village" : null,
        "cityCouncil": {
            "name": null,
            "district": "4",
            "map": null
        },
        "electricUtility": {
            "name": "Consolidated Edison",
            "district": "7",
            "map": null
        }
      },
      "multiMatch" : false,
      "statusCode" : 0,
      "description" : "Success."
    }

The ``districtAssigned`` field will be true if any districts were assigned.
The ``senateAssigned`` field will be true if the senate district was assigned.

The main components of the response are ``address``, ``geocode``, and ``districts``. The ``address`` typically contains
a corrected address response from the geocode provider or is the result of USPS correction if ``uspsValidated`` is true.
The ``geocode`` contains the coordinates that were used to perform district assignment. The ``districts`` object contains all
the district types supported by the service. ``district`` refers to the code or number that represents the district.

The ``matchLevel`` indicates how granular the district assignment is. The following table describes the different ``matchLevel`` values:

+------------------+-----------------------------------------------------------------+
| matchLevel value | Description                                                     |
+==================+=================================================================+
| HOUSE            | A specific house/bldg address was located and assigned.         |
+------------------+-----------------------------------------------------------------+
| STREET           | A street range was assigned within the given city or zip5.      |
+------------------+-----------------------------------------------------------------+
| ZIP5             | A zip5 region was assigned.                                     |
+------------------+-----------------------------------------------------------------+
| CITY             | The union of zip5 regions that belong to a city were assigned.  |
+------------------+-----------------------------------------------------------------+

If the ``matchLevel`` is not HOUSE, the ``multiMatch`` field will be true. The assigned districts for a non HOUSE match will
be those that completely encompass that geographic region. If a street spans multiple senate districts, it will not have it's
senate district assigned. However if a street is contained fully by a senate district, it will have been assigned with that district.
The same applies to ZIP5 and CITY level lookups. Optionally the ``showMultiMatch`` parameter can be set to true to view all possible
overlap boundaries and street ranges. The output of that is described below.

Map polygon data can be retrieved for purposes of rendering to a 3rd party mapping application.

If ``showMaps`` is set to true::

    /api/v2/district/assign?addr=280 Madison Ave, NY&showMaps=true

each district portion of the response will include geometry data in ``map.geom``, and a geometry data type under ``map.type``.
Any district that does not have any map data associated with it will have ``map`` : null;

.. tip::
     'geom' is an array containing an array of coordinate pairs which are represented as two floats in an array,
     e.g geom[0] -> array of coordinate pairs (represented as [lat, lon]) of the first polygon.

District assignment via coordinate pairs is also supported::

    /api/v2/district/assign?lat=40.751352&lon=-73.980335

The supplied point will automatically be reverse geocoded by the service and will match the response given by an address input.

BluebirdCRM
^^^^^^^^^^^

For integration with Bluebird CRM, the bluebird method can be used instead::

    Unparsed Format:
    /api/v2/district/bluebird?addr=280 Madison Ave NY

    Parsed Format:
    /api/v2/district/bluebird?addr1=280 Madison Ave&state=NY

Bluebird district assign is similar to the default assign except that these options are fixed::

    districtSource: missing
    geocoder: missing
    uspsValidate: true

The response is identical to that of a default district assign with those parameters.

Batch District Assign
^^^^^^^^^^^^^^^^^^^^^

Both Assign_ and BluebirdCRM_ methods can handle batch requests as well. The format is::

    /api/v2/district/assign/batch
    /api/v2/district/bluebird/batch

The addresses must be JSON encoded and sent along the POST payload in the same way as :ref:`batch-geocode`.

A sample batch district assign in PHP::

    <?php

    $addresses = array(
            array(
                    "addr1" => "100 Nyroy Dr",
                    "city" => "Troy",
                    "state" => "NY",
                    "zip5" => "12180"
            ),
            array(
                    "addr1" => "44 Fairlawn Ave",
                    "city" => "Albany",
                    "state" => "NY",
                    "zip5" => "12203"
            )
    );

    $post_body = json_encode($addresses);

    $ch = curl_init("http://localhost:8080/GeoApi/api/v2/district/assign/batch");
    curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
    curl_setopt($ch, CURLOPT_POSTFIELDS, $post_body);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
            'Content-Type: application/json',
            'Content-Length: ' . strlen($post_body)
    ));

    $result = curl_exec($ch);

    print_r($result);

And the response::

    {
      "results" : [ {
        "status" : "SUCCESS",
        "source" : "StreetFile",
        "messages" : [ ],
        "address" : {
          "addr1" : "100 Nyroy Dr",
          "addr2" : "",
          "city" : "Troy",
          "state" : "NY",
          "zip5" : "12180",
          "zip4" : "1921"
        },
        "geocode" : {
          "lat" : 42.741112,
          "lon" : -73.668762,
          "quality" : "HOUSE",
          "method" : "YahooDao"
        },
        "geocoded" : true,
        "districtAssigned" : true,
        "senateAssigned" : true,
        "uspsValidated" : false,
        "matchLevel" : "HOUSE",
        "districts" : {
          "senate" : {
            "name" : "NY Senate District 44", "district" : "44", "senator" : null
          },
          "congressional" : {
            "name" : "NY Congressional District 20", "district" : "20", "member" : null
          },
          "assembly" : {
            "name" : "NY Assembly District 107", "district" : "107", "member" : null
          },
          "county" : {
            "name" : null, "district" : "38"
          },
          "election" : {
            "name" : null, "district" : "12"
          },
          "school" : null,
          "town" : {
            "name" : null, "district" : "-TROY"
          },
          "zip" : {
            "name" : null, "district" : "12180"
          },
          "cleg" : {
            "name" : null, "district" : "1"
          },
          "ward" : {
            "name" : null, "district" : null
          },
          "village" : {
            "name" : null, "district" : null
          }
        },
        "multiMatch" : false,
        "statusCode" : 0,
        "description" : "Success."
      }, {
        "status" : "SUCCESS",
        "source" : "StreetFile",
        "messages" : [ ],
        "address" : {
          "addr1" : "44 Fairlawn Ave",
          "addr2" : "",
          "city" : "Albany",
          "state" : "NY",
          "zip5" : "12203",
          "zip4" : "1933"
        },
        "geocode" : {
          "lat" : 42.670583,
          "lon" : -73.799606,
          "quality" : "HOUSE",
          "method" : "YahooDao"
        },
        "geocoded" : true,
        "districtAssigned" : true,
        "senateAssigned" : true,
        "uspsValidated" : false,
        "matchLevel" : "HOUSE",
        "districts" : {
          "senate" : {
            "name" : "NY Senate District 44", "district" : "44", "senator" : null
          },
          "congressional" : {
            "name" : "NY Congressional District 20", "district" : "20", "member" : null
          },
          "assembly" : {
            "name" : "NY Assembly District 109", "district" : "109", "member" : null
          },
          "county" : {
            "name" : null, "district" : "1"
          },
          "election" : {
            "name" : null, "district" : "5"
          },
          "school" : null,
          "town" : {
            "name" : null, "district" : "-ALBAN"
          },
          "zip" : {
            "name" : null, "district" : "12203"
          },
          "cleg" : {
            "name" : null, "district" : "13"
          },
          "ward" : {
            "name" : null, "district" : "13"
          },
          "village" : {
            "name" : null, "district" : null
          }
        },
        "multiMatch" : false,
        "statusCode" : 0,
        "description" : "Success."
      } ],
     "total" : 2
    }

And a parse error response in case of invalid input::

    {
      "status": "INVALID_BATCH_ADDRESSES",
      "statusCode": 55,
      "description": "The supplied batch address list could not be parsed."
    }

.. note: Batch district assignment can be configured by the application to follow a different strategy for
         purposes of improving performance. In this example the configuration was set to use street files only
         hence why many of the results are partial successes. However this behaviour can be changed using
         the same query parameters as the single request version.

Street
------

The street_ service provides a comprehensive list of street address range to district mappings. This data is compiled from
Board of Election Street File data and can be searched via zip code.

The street_ service has the following method(s):

+-------------+-----------------------------------------------------------------+
| Method      | Description                                                     |
+=============+=================================================================+
| lookup      | Retrieve street information via zipcode.                        |
+-------------+-----------------------------------------------------------------+

A request is of the following form::

    /api/v2/street/lookup?zip5=12210

The response is::

    {
      "status" : "SUCCESS",
      "source" : "STREETFILE",
      "streets" : [ {
        "bldgLoNum": 2,
        "bldgHiNum": 18,
        "street": "1ST ST",
        "location": "ALBANY",
        "zip5": "12210",
        "parity": "EVENS",
        "congressional": "20",
        "assembly": "109",
        "election": "1",
        "town": "-ALBAN",
        "senate": "46",
        "county": "1"
      },
      {
        "bldgLoNum": 24,
        "bldgHiNum": 28,
        "street": "1ST ST",
        "location": "ALBANY",
        "zip5": "12210",
        "parity": "EVENS",
        "congressional": "20",
        "assembly": "109",
        "election": "1",
        "town": "-ALBAN",
        "senate": "46",
        "county": "1"
      },
      ... ]
    }

An invalid response, typically due to a non matching zip code is::

    {
      "status" : "NO_STREET_LOOKUP_RESULT",
      "source" : "STREETFILE",
      "messages" : [ ],
      "streets" : [ ],
      "statusCode" : 74,
      "description" : "Street lookup returned no results for the given zip5"
    }

Map
---

The map_ service provides geometry information for certain district types. The methods for this service
actually represent the district type to retrieve maps for. The available types are listed at /api/v2/geo/types.

The parameters are:

+---------------+------------------------------------------------------------------------------------+
| Params        | Description                                                                        |
+===============+====================================================================================+
| district      | Specify the district code. If unspecified, all districts will be retrieved.        |
+---------------+------------------------------------------------------------------------------------+
| showMembers   | If true: senator, assembly member, and congressional member data will be appended. |
+---------------+------------------------------------------------------------------------------------+
| meta          | If true, doesn't return map geometry data.        |
+---------------+------------------------------------------------------------------------------------+

To retrieve map and member data for all senate districts::

/api/v2/map/senate?showMembers=true

To retrieve map data for just senate district 1::

/api/v2/map/senate?district=1

The response of the second query is::

    {
      "status" : "SUCCESS",
      "source" : "SHAPEFILE",
      "map" : {
        "geom" : (truncated)
        "type" : "Polygon"
      },
      "name": "Senate District 1",
      "type": "SENATE",
      "link": null,
      "member": null,
      "district": "1",
      "statusCode": 0,
      "description": "Success."
    }

The member data for senate, assembly, and congressional districts will have the same senator output as in district assignment.

Status Codes
~~~~~~~~~~~~

ResultStatus.java lists all status codes. A positive response will have a status code of 0, while the rest are error statuses.

.. toctree::
   :maxdepth: 2
