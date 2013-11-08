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

All API requests need to be validated using an assigned API key however it is not required when requests are made from
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

Address, Geo, and District API calls allow for specifying a ``provider`` to carry out the request::

    /api/v2/<group>/<method>?provider=PROVIDER_NAME

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
| zipcode     | Lookup the zipcode given a street address   |
+-------------+---------------------------------------------+

The available providers are:

+-------------+---------------------------------------------+
| Provider    | Description                                 |
+=============+=============================================+
| usps        | USPS AMS Address Correction                 |
+-------------+---------------------------------------------+
| uspsais     | USPS AIS Address Correction                 |
+-------------+---------------------------------------------+

The usage of ``validate`` with an address input::

    /api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY

The validated response::

    {
      "status" : "SUCCESS",
      "source" : "USPSAMS",
      "messages" : [ ],
      "address" : {
        "addr1" : "44 Fairlawn Ave",
        "addr2" : "",
        "city" : "Albany",
        "state" : "NY",
        "zip5" : "12203",
        "zip4" : "1914"
      },
      "validated" : true,
      "statusCode" : 0,
      "description" : "Success."
    }

A failed validation response::

    {
      "status" : "NO_ADDRESS_VALIDATE_RESULT",
      "source" : "USPSAMS",
      "messages" : [ ],
      "address" : null,
      "validated" : false,
      "statusCode" : 73,
      "description" : "The address could not be validated."
    }

.. caution:: USPS address validation requires addr1, city and state explicitly specified in the query parameters. Given a query that
          is missing those fields, USPS will not be used to perform validation and another provider will be used instead.

The ``punct`` parameter can be supplied if abbreviations require a period appended to them. Simply add ``punct=true`` to the url
to enable punctuation.

The usage of ``citystate`` with a zip code input::

    /api/v2/address/citystate?zip5=12210

The city/state response::

    {
      "status" : "SUCCESS",
      "source" : "USPSAMS",
      "messages" : [ ],
      "city" : "ALBANY",
      "state" : "NY",
      "zip5" : "12210",
      "statusCode" : 0,
      "description" : "Success."
    }

A failed city/state response with invalid input::

    {
      "status" : "NO_ADDRESS_VALIDATE_RESULT",
      "source" : "USPSAMS",
      "messages" : [ "Invalid Zip Code." ],
      "city" : "",
      "state" : "",
      "zip5" : "",
      "statusCode" : 73,
      "description" : "The address could not be validated."
    }

The usage of ``zipcode``::

    /api/v2/address/zipcode?addr1=44 Fairlawn Avenue&city=Albany&state=NY

The zipcode response::

    {
      "status" : "SUCCESS",
      "source" : "USPSAMS",
      "messages" : [ ],
      "zip5" : "12203",
      "zip4" : "1914",
      "statusCode" : 0,
      "description" : "Success."
    }

A failed zipcode response, similar to the failed validate response::

    {
      "status" : "NO_ADDRESS_VALIDATE_RESULT",
      "source" : "USPSAMS",
      "messages" : [ ],
      "zip5" : null,
      "zip4" : null,
      "statusCode" : 73,
      "description" : "The address could not be validated."
    }

.. note:: Zipcode lookup has the same USPS constraints as the validate method

To force the request to use a certain provider supply the query parameter ``provider``::

    /api/v2/address/<method>?<params..>&provider=usps
    /api/v2/address/<method>?<params..>&provider=uspsais

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

The available providers ordered from most accurate to least are:

+-------------+---------------------------------------------+-------------+
| Provider    | Description                                 | API Limits  |
+=============+=============================================+=============+
| yahoo       | Free YQL service from Yahoo. (recommended)  | ~20000 day  |
+-------------+---------------------------------------------+-------------+
| mapquest    | Free MapQuest geocoding service             | 5000 day?   |
+-------------+---------------------------------------------+-------------+
| tiger       | In-database geocoding using census data     | Unlimited   |
+-------------+---------------------------------------------+-------------+
| ruby        | Ruby implementation using census data       | Unlimited   |
+-------------+---------------------------------------------+-------------+
| osm         | Open Street Maps API                        | 1 per sec?  |
+-------------+---------------------------------------------+-------------+

Methods have the following optional parameters:

+-------------+-------------------------------------------------------------------------------+
| Param       | Description                                                                   |
+=============+===============================================================================+
| provider    | Specify which geocode provider to use first (see above table)                 |
+-------------+-------------------------------------------------------------------------------+
| useFallback | If false and provider is set, only the provider will be used for the request. |
+-------------+-------------------------------------------------------------------------------+

For example to use just yahoo without falling back to other providers in case of error::

    /api/v2/geo/<method>?<params..>&provider=yahoo&useFallback=false

Geocode
^^^^^^^

The usage of ``geocode`` with an address input::

    /api/v2/geo/geocode?addr=200 State St, Albany NY 12210
    /api/v2/geo/geocode?addr1=200 State St&city=Albany&state=NY&zip5=12210

The geocode response::

    {
      "status" : "SUCCESS",
      "source" : "GeoCache",
      "messages" : [ ],
      "address" : {
        "addr1" : "200 State St",
        "addr2" : "",
        "city" : "Albany",
        "state" : "NY",
        "zip5" : "12210",
        "zip4" : ""
      },
      "geocode" : {
        "lat" : 42.65203,
        "lon" : -73.75759,
        "quality" : "HOUSE",
        "method" : "YahooDao"
      },
      "geocoded" : true,
      "statusCode" : 0,
      "description" : "Success."
    }

The ``source`` indicates where the response was returned from whereas ``geocode.method`` indicates where the geocode was computed.

The ``address`` is typically a normalized representation of the input address but it depends on the geocode provider used.

The ``geocode.quality`` metric indicates the accuracy/confidence level of the geocode. A successful geocode response will have
one of the following quality levels ordered from most accurate to least:

* POINT
* HOUSE
* ZIP_EXT
* STREET
* ZIP

An unsuccessful response will resemble the following::

    {
      "status" : "NO_GEOCODE_RESULT",
      "source" : "TigerGeocoder",
      "messages" : [ ],
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

    api/v2/geo/revgeocode?lat=42.652030&lon=-73.757590

The reverse geocode response::

    {
      "status" : "SUCCESS",
      "source" : "Yahoo",
      "messages" : [ ],
      "address" : {
        "addr1" : "200 State St",
        "addr2" : "",
        "city" : "Albany",
        "state" : "NY",
        "zip5" : "12210",
        "zip4" : ""
      },
      "geocode" : {
        "lat" : 42.65204,
        "lon" : -73.757602,
        "quality" : "HOUSE",
        "method" : "YahooDao"
      },
      "revGeocoded" : true,
      "statusCode" : 0,
      "description" : "Success."
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
| shapefile   | In-database district shapes.         | Geocode          |
+-------------+--------------------------------------+------------------+
| streetfile  | Street file database.                | Address          |
+-------------+--------------------------------------+------------------+
| geoserver   | Geoserver hosted shape files         | Geocode          |
+-------------+--------------------------------------+------------------+

``assign`` has the following optional parameters:

+------------------+-------------------------------------------------------------------------------+
| Param            | Description                                                                   |
+==================+===============================================================================+
| provider         | Specify which district provider to use. Overrides 'districtStrategy'          |
+------------------+-------------------------------------------------------------------------------+
| geoProvider      | Specify which geocode provider to use.                                        |
+------------------+-------------------------------------------------------------------------------+
| showMembers      | If true: senator, assembly, and congressional member data is appended.        |
+------------------+-------------------------------------------------------------------------------+
| showMaps         | If true: map data is appended for each district.                              |
+------------------+-------------------------------------------------------------------------------+
| showMultiMatch   | If true: street range and overlap data will be returned for multi-matches.    |
+------------------+-------------------------------------------------------------------------------+
| uspsValidate     | If true: usps will be used to perform address correction.                     |
+------------------+-------------------------------------------------------------------------------+
| skipGeocode      | If true: no geocoding will occur, useful when provider=streetfile             |
+------------------+-------------------------------------------------------------------------------+
| districtStrategy | Specify the strategy to use for district assignment (see below)               |
+------------------+-------------------------------------------------------------------------------+

The following district strategies can be utilized:

+------------------+-------------------------------------------------------------------------------------+
| Strategy         | Description                                                                         |
+==================+=====================================================================================+
| neighborMatch    | Perform shape and street lookup, consolidating only when proximity condition met.   |
+------------------+-------------------------------------------------------------------------------------+
| streetFallback   | Perform shape and street lookup, using street file in case of mismatch.             |
+------------------+-------------------------------------------------------------------------------------+
| shapeFallback    | Perform street lookup and only fall back to shape files when street lookup failed.  |
+------------------+-------------------------------------------------------------------------------------+
| streetOnly       | Perform street lookup only.                                                         |
+------------------+-------------------------------------------------------------------------------------+

Unlike the ``geo`` service, specifying a ``provider`` or ``geoProvider`` will by default disable any fallback. If the provider
is not specified the service will utilize multiple providers to provide the most accurate result. If the geoProvider is not
specified, the service will iterate through a series of providers if needed until a geocode match is obtained. Specifying
provider or geoProvider is not recommended as it may reduce the accuracy of the results.

.. caution:: USPS validation will only work when addr1, city, and state are provided. See address section above for details.

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
      "source" : "DistrictServiceProvider",
      "messages" : [ ],
      "address" : {
        "addr1" : "280 Madison Ave",
        "addr2" : "",
        "city" : "New York",
        "state" : "NY",
        "zip5" : "10016",
        "zip4" : "0802"
      },
      "geocode" : {
        "lat" : 40.751352,
        "lon" : -73.980335,
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
          "name" : "NY Senate District 27",
          "district" : "27",
          "senator" : null
        },
        "congressional" : {
          "name" : "NY Congressional District 12",
          "district" : "12",
          "member" : null
        },
        "assembly" : {
          "name" : "NY Assembly District 73",
          "district" : "73",
          "member" : null
        },
        "county" : {
          "name" : "New York County",
          "district" : "62"
        },
        "election" : {
          "name" : null,
          "district" : "6"
        },
        "school" : {
          "name" : "Manhattan School District",
          "district" : "369"
        },
        "town" : {
          "name" : "New York",
          "district" : "-NYC"
        },
        "zip" : {
          "name" : "10016",
          "district" : "10016"
        },
        "cleg" : null,
        "ward" : null,
        "village" : null
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

If ``showMultiMatch`` is set to true::

    (Note that just the street is supplied to trigger a matchLevel of STREET)
    /api/v2/district/assign?addr=Madison Ave, NY&showMultiMatch=true

The response will resemble the following (truncated for brevity)::

    {
      "status" : "SUCCESS",
      "source" : "DistrictShapefile",
      "messages" : [ ],
      "address" : {
        "addr1" : "Madison Ave",
        "addr2" : "",
        "city" : "New York",
        "state" : "NY",
        "zip5" : "10010",
        "zip4" : ""
      },
      "geocode" : {
        "lat" : 40.742119,
        "lon" : -73.987076,
        "quality" : "STREET",
        "method" : "YahooDao"
      },
      "geocoded" : true,
      "districtAssigned" : true,
      "senateAssigned" : false,
      "uspsValidated" : false,
      "matchLevel" : "STREET",
      "districts" : {
        "senate" : null,
        "congressional" : null,
        "assembly" : null,
        "county" : {
          "name" : "New York County",
          "district" : "62"
        },
        "election" : null,
        "school" : null,
        "town" : null,
        "zip" : null,
        "cleg" : null,
        "ward" : null,
        "village" : null
      },
      "overlaps" : {
        "assembly" : [ {
          "name" : null,
          "district" : "68",
          "intersectionArea" : 6240510.4531401154,
          "areaPercentage" : 0.11
        }, {
          "name" : null,
          "district" : "70",
          "intersectionArea" : 4577779.5892158523,
          "areaPercentage" : 0.08
        }, {
          "name" : null,
          "district" : "73",
          "intersectionArea" : 4175298.2836983129,
          "areaPercentage" : 0.07
        }, {
          "name" : null,
          "district" : "75",
          "intersectionArea" : 6626512.6820231033,
          "areaPercentage" : 0.12
        } ],
        "congressional" : [ {
          "name" : null,
          "district" : "13",
          "intersectionArea" : 18492568.968741294,
          "areaPercentage" : 0.33
        }, {
          "name" : null,
          "district" : "12",
          "intersectionArea" : 17861734.376638968,
          "areaPercentage" : 0.32
        } ],
        "senate" : [ {
          "name" : "NY Senate District 30",
          "district" : "30",
          "intersectionArea" : 9377715.5224368814,
          "areaPercentage" : 0.17
        }, {
          "name" : "NY Senate District 27",
          "district" : "27",
          "intersectionArea" : 11881916.930670122,
          "areaPercentage" : 0.21
        }, {
          "name" : "NY Senate District 28",
          "district" : "28",
          "intersectionArea" : 7893341.056081377,
          "areaPercentage" : 0.14
        }, {
          "name" : "NY Senate District 29",
          "district" : "29",
          "intersectionArea" : 5834177.0993249146,
          "areaPercentage" : 0.10
        } ]
      },
      "totalReferenceArea" : 55823719.26265686,
      "areaUnit" : "SQ_METERS",
      "streets" : [ {
        "bldgLoNum" : 1,
        "bldgHiNum" : 43,
        "street" : "MADISON AVE",
        "location" : "MANHATTAN",
        "zip5" : "10010",
        "parity" : "ODDS",
        "congressional" : "12",
        "senate" : "28",
        "assembly" : "75",
        "county" : "62",
        "election" : "30",
        "town" : null
      }, {
        "bldgLoNum" : 2,
        "bldgHiNum" : 2,
        "street" : "MADISON AVE",
        "location" : "MANHATTAN",
        "zip5" : "10010",
        "parity" : "EVENS",
        "congressional" : "12",
        "senate" : "28",
        "assembly" : "75",
        "county" : "62",
        "election" : "29",
        "town" : null
      }, (114 more entries..) ],
      "multiMatch" : true,
      "statusCode" : 0,
      "description" : "Success."
    }

Any districts that were successfully assigned (fully contain the specified street/city/zip) will appear under the ``districts`` field
as previously described. If there is contention between multiple districts of a certain type (senate, assembly, etc) they will be
enumerated within the ``overlaps`` field. The ``areaPercentage`` field provides a rough estimate of the geographic area it occupies
relative to the other districts of the given type. The ``areaPercentage`` is only applicable for CITY and ZIP5 matchLevel responses.

The ``streets`` field is applicable when matchLevel = STREET. It is an array that contains objects representing discrete street ranges
and the associated districts within that range. This information can be used to determine where district boundaries change within a given
street. If the zip5 is specified in the input, the range will be limited to that zip. Otherwise the range will be contained within the
zip boundaries the represent the city.

Map polygon data can be retrieved for purposes of rendering to a 3rd party mapping application.

If ``showMaps`` is set to true::

    /api/v2/district/assign?addr=280 Madison Ave, NY&showMaps=true

the ``districts`` portion of the response will be different::

    ...
      "districts" : {
        "senate" : {
          "name" : "State Senate District 27",
          "district" : "27",
          "map" : {
            "geom" : [[[ <lat>, <lon>], ]]
          },
          "nearBorder" : true,
          "neighbors" : [ {
            "name" : "State Senate District 28",
            "district" : "28",
            "member" : null,
            "map" : {
              "geom" : [[[ <lat>, <lon>], ]]
            }
          } ],
          "senator" : null
        },
        "congressional" : {
          "name" : "State Congressional District 12",
          "district" : "12",
          "map" : {
            "geom" : [[[ <lat>, <lon>], ]]
          },
          "nearBorder" : false,
          "neighbors" : [ ],
          "member" : null
        },
        "assembly" : {
          "name" : "State Assembly District 73",
          "district" : "73",
          "map" : {
            "geom" : [[[ <lat>, <lon>], ]]
          },
          "nearBorder" : true,
          "neighbors" : [ {
            "name" : null,
            "district" : "75",
            "member" : null,
            "map" : {
              "geom" : [[[ <lat>, <lon>], ]]
            }
          }, {
            "name" : null,
            "district" : "74",
            "member" : null,
            "map" : {
              "geom" : [[[ <lat>, <lon>], ]]
            }
          } ],
          "member" : null
        },
        "county" : {
          "name" : "New York County",
          "district" : "62",
          "map" : {
            "geom" : [[[ <lat>, <lon>], ]]
          },
          "nearBorder" : false,
          "neighbors" : [ ]
        },
        "election" : {
          "name" : null,
          "district" : "6",
          "map" : null,
          "nearBorder" : false,
          "neighbors" : [ ]
        },
        "school" : {
          "name" : "Manhattan School District",
          "district" : "369",
          "map" : {
            "geom" : [[[ <lat>, <lon>], ]]
           },
          "nearBorder" : false,
          "neighbors" : [ ]
        },
        "town" : {
          "name" : "New York",
          "district" : "-NYC",
          "map" : {
            "geom" : [[[ <lat>, <lon>], ]]
          },
          "nearBorder" : false,
          "neighbors" : [ ]
        },
        "cleg" : {
          "name" : null,
          "district" : null,
          "map" : null,
          "nearBorder" : false,
          "neighbors" : [ ]
        },
        "ward" : {
          "name" : null,
          "district" : null,
          "map" : null,
          "nearBorder" : false,
          "neighbors" : [ ]
        },
        "village" : {
          "name" : null,
          "district" : null,
          "map" : null,
          "nearBorder" : false,
          "neighbors" : [ ]
        }
      },
      ...

``map.geom`` will contain polygon data if ``showMaps`` is true. ``nearBorder`` indicates that the address is very close
to a district boundary. If ``nearBorder`` is true it suggests that the result for that district may be not be certain although
by default the service will automatically attempt to correct the uncertain districts. ``neighbors`` will contain a list of
districts that are near the geocode location but for performance reasons will only be populated if ``nearBorder`` was true
for that district. The application may also limit neighbor matches to just the senate district to improve performance.
Any district that does not have any map data associated with it will have ``map`` : null;

.. tip::
     'geom' is an array containing an array of coordinate pairs which are represented as two floats in an array,
     e.g geom[0] -> array of coordinate pairs (represented as [lat, lon]) of the first polygon.

.. note::
     Not all district types have polygon data available. Currently only senate, assembly, congressional, county, town,
     and school maps are available. However the other district types may be supported in the future.

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

Bluebird district assign is similar to the default assign except that the options are fixed::

    showMaps:         false
    showMembers:      false
    skipGeocode:      false
    uspsValidate:     true
    showMultiMatch:   false
    districtStrategy: (application setting)

The response is identical to that of a default district assign with those parameters. The
district assignment strategy for the bluebird method can be configured by the application so that
district assignment for ``district/assign`` and ``district/bluebird`` can follow different execution paths, either to favor
performance or completeness.

An unsuccessful district assign response will look similar to the following::

    {
      "status" : "NO_DISTRICT_RESULT",
      "source" : "StreetFile",
      "messages" : [ ],
      "address" : null,
      "geocode" : null,
      "districts" : null,
      "geocoded" : false,
      "uspsValidated" : false,
      "districtAssigned" : false,
      "senateAssigned" : false,
      "statusCode" : 70,
      "description" : "District assignment returned no results."
    }

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
      "source": "DistrictController",
      "messages": [],
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
      "source" : "StreetController",
      "messages" : [ ],
      "streets" : [ {
        "bldgLoNum" : 2,
        "bldgHiNum" : 40,
        "street" : "1 ST",
        "location" : "ALBANY",
        "zip5" : "12210",
        "senate" : "44",
        "congressional" : "20",
        "assembly" : "108",
        "county" : "1",
        "election" : "7",
        "town" : "-ALBAN"
      }, {
        "bldgLoNum" : 5,
        "bldgHiNum" : 191,
        "street" : "1 ST",
        "location" : "ALBANY",
        "zip5" : "12210",
        "senate" : "44",
        "congressional" : "20",
        "assembly" : "108",
        "county" : "1",
        "election" : "7",
        "town" : "-ALBAN"
      },
      ... ]
    }

An invalid response, typically due to a non matching zip code is::

    {
      "status" : "NO_STREET_LOOKUP_RESULT",
      "source" : "StreetController",
      "messages" : [ ],
      "streets" : [ ],
      "statusCode" : 74,
      "description" : "Street lookup returned no results for the given zip5"
    }

Map
---

The map_ service provides geometry information for certain district types. The methods for this service
actually represent the district type to retrieve maps for. The available methods are:

+---------------+--------------------------------+
| Method        | Description                    |
+===============+================================+
| senate        | NY State Senate Maps           |
+---------------+--------------------------------+
| assembly      | NY State Assembly Maps         |
+---------------+--------------------------------+
| congressional | NY State Congressional Maps    |
+---------------+--------------------------------+
| county        | NY County Maps                 |
+---------------+--------------------------------+
| town          | NY Town Maps                   |
+---------------+--------------------------------+
| school        | NY School District Maps        |
+---------------+--------------------------------+

The parameters are:

+---------------+------------------------------------------------------------------------------------+
| Params        | Description                                                                        |
+===============+====================================================================================+
| district      | Specify the district code. If unspecified, all districts will be retrieved.        |
+---------------+------------------------------------------------------------------------------------+
| showMembers   | If true: senator, assembly member, and congressional member data will be appended. |
+---------------+------------------------------------------------------------------------------------+

To retrieve map and member data for all senate districts::

/api/v2/map/senate?showMembers=true

To retrieve map data for just senate district 1::

/api/v2/map/senate?district=1

The response of the second query is::

    {
      "status" : "SUCCESS",
      "source" : "DistrictShapefile",
      "messages" : [ ],
      "map" : {
        "geom" : [ [ [ 41.052309, -73.141594 ],  (truncated) ]]
      },
      "member" : null,
      "district" : "1",
      "name" : "State Senate District 1",
      "type" : "SENATE",
      "statusCode" : 0,
      "description" : "Success."
    }

The member data for senate districts will have the same senator output as in district assignment::

    "member" : {
        "name" : "Kenneth P. LaValle",
        "lastName" : "LaValle",
        "shortName" : "lavalle",
        "email" : "lavalle@nysenate.gov",
        "additionalContact" : "",
        "imageUrl" : "http://www.nysenate.gov/files/profile-pictures/NewHeadShotLavalle2.jpg",
        "url" : "http://www.nysenate.gov/senator/kenneth-p-lavalle",
        "partyAffiliations" : [ "R" ],
        "offices" : [ {
          "name" : "Albany Office",
          "street" : "188 State Street",
          "city" : "Albany",
          "postalCode" : "12247",
          "provinceName" : "New York",
          "province" : "NY",
          "countryName" : "United States",
          "country" : "us",
          "phone" : "(518) 455-3121",
          "fax" : "",
          "otherPhone" : "",
          "additional" : "Room 806, Legislative Office Building",
          "latitude" : 42.652855,
          "longitude" : -73.759091
        }, {
          "name" : "District Office",
          "street" : "28 North Country Rd",
          "city" : "Mount Sinai",
          "postalCode" : "11766",
          "provinceName" : "New York",
          "province" : "NY",
          "countryName" : "United States",
          "country" : "us",
          "phone" : "(631) 473-1461",
          "fax" : "(631) 473-1513",
          "otherPhone" : "",
          "additional" : "Suite 203",
          "latitude" : 40.938617,
          "longitude" : -73.035149
        } ],
        "district" : {
          "number" : 1,
          "url" : "http://www.nysenate.gov/district/01",
          "imageUrl" : "http://www.nysenate.gov/files/sd1_1.jpg",
          "mapUrl" : "http://geo.nysenate.gov/maps/regular.jsp?x=850&y=595&sd=01"
        },
        "social" : {
          "twitter" : "http://twitter.com/senatorlavalle",
          "youtube" : "http://www.youtube.com/user/KPLSenate",
          "myspace" : "",
          "picasa" : "",
          "flickr" : "",
          "facebook" : "https://www.facebook.com/kenneth.p.lavalle"
        }
    }

Sample member data for assembly and congressional districts::

    "member" : {
        "name" : "Thiele, Jr., Fred ",
        "url" : "http://assembly.state.ny.us/mem/Fred-W-Thiele-Jr"
    }

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















