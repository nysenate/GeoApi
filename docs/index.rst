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
| options_    | List the available providers                |
+-------------+---------------------------------------------+

The available providers are:

+-------------+---------------------------------------------+
| Provider    | Description                                 |
+=============+=============================================+
| AMS         | USPS AMS Address Correction                 |
+-------------+---------------------------------------------+
| AIS         | USPS AIS Address Correction                 |
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
| options_    | List the available geocoders                |
+-------------+---------------------------------------------+

The available geocoders are:

+-------------+---------------------------------------------+
| Geocoder    | Description                                 |
+=============+=============================================+
| GEOCACHE    | Cached geocodes                             |
+-------------+---------------------------------------------+
| NYSGEO      | NYS geocoding service                       |
+-------------+---------------------------------------------+
| GOOGLE      | Paid geocoding service                      |
+-------------+---------------------------------------------+

The ``geocode`` and ``revgeocode`` methods have the following optional parameters:

+-------------+---------------------------------------------+
| Param       | Description                                 |
+=============+=============================================+
| geocoder    | Specify which geocoder to use               |
+-------------+---------------------------------------------+

For example to use just NYSGEO::

    /api/v2/geo/<method>?<params..>&geocoder=NYSGEO

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

The addresses must be JSON encoded and sent in the POST request payload.
The fields are identical to the query parameter fields for an address.

Unlike the single request version, the batch call takes no parameters. It always uses the default ranked
fallback, trying each geocoder in turn until a match is obtained.

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
| info_       | List every district of one type along with its attributes       |
+-------------+-----------------------------------------------------------------+
| options_    | List the available district sources                             |
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
| districtSource   | Specify which district source to use.                                         |
+------------------+-------------------------------------------------------------------------------+
| geocoder         | Specify which geocoder to use.                                                |
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

.. _district-identifiers:

District Identifiers
^^^^^^^^^^^^^^^^^^^^

Wherever a district appears in a response - district assignment, map_ data, or info_ - it is described by the
same three fields:

+-------------+-------------------------------------------------------------------------------------+
| Field       | Description                                                                         |
+=============+=====================================================================================+
| id          | SAGE's identifier for the district. Unique within the district type.                |
+-------------+-------------------------------------------------------------------------------------+
| district    | The display code: the short code or number this district is known by publicly.      |
+-------------+-------------------------------------------------------------------------------------+
| name        | The human readable name of the district.                                            |
+-------------+-------------------------------------------------------------------------------------+

The ``id`` is one or more groups of digits joined by hyphens. It is what SAGE keys geometry and district
attributes on, and it is the value to hand back to the API when a request needs to name a district: the
``district`` parameter of a map_ request takes an ``id``, not a display code.

For most types the ``id`` and the ``district`` code are the same string. They differ when the natural public
code is not what SAGE stores districts under - counties are keyed on their GNIS id but publicly known by a
county code, and towns and cities are keyed on their GNIS id but carry a short abbreviation as their code.
The hyphenated form exists because some types have no single identifying number: an election district's
``id`` is its municipality, assembly district, village, ward, county legislative district, and ED number
joined together, since the ED number by itself repeats across the state.

+------------+--------------------+------------+------------------------------+
| Type       | id                 | district   | name                         |
+============+====================+============+==============================+
| senate     | ``28``             | ``28``     | Senate District 28           |
+------------+--------------------+------------+------------------------------+
| county     | ``974129``         | ``62``     | New York County              |
+------------+--------------------+------------+------------------------------+
| town       | ``2395220``        | ``-NYC``   | New York City                |
+------------+--------------------+------------+------------------------------+
| election   | ``978659-1-1``     | ``1``      | City of Albany, Ward 1 ED 1  |
+------------+--------------------+------------+------------------------------+

District objects in an assignment response additionally carry a ``displayName``, which names the district
*type* rather than the district ("Senate", "Town/City", "Electric Utility").

.. note:: Election districts assigned from the streetfile source are identified by their ED number alone,
          since that is all the Board of Elections data carries. Only the shapefile source yields the fully
          qualified election ``id``, and only such an id resolves to a ``name``.

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
      "sources" : "STREETFILE, SHAPEFILE",
      "address" : {
        "addr1" : "280 Madison Ave",
        "addr2" : "",
        "city" : "New York",
        "state" : "NY",
        "zip5" : "10016",
        "zip4" : "0801"
      },
      "geocode" : {
        "lat" : "40.7514214",
        "lon" : "-73.9805145",
        "quality" : "HOUSE",
        "method" : "GOOGLE",
        "cached" : true,
        "openLocCode" : "87G8Q229+HQ"
      },
      "geocoded" : true,
      "districtAssigned" : true,
      "senateAssigned" : true,
      "uspsValidated" : true,
      "matchLevel" : "HOUSE",
      "districts" : {
        "senate" : {
          "id" : "28",
          "district" : "28",
          "name" : "Senate District 28",
          "member" : (excluded for length),
          "displayName" : "Senate"
        },
        "assembly" : {
          "id" : "73",
          "district" : "73",
          "name" : "Assembly District 73",
          "member" : (excluded for length),
          "displayName" : "Assembly"
        },
        "congressional" : {
          "id" : "12",
          "district" : "12",
          "name" : "Congressional District 12",
          "member" : (excluded for length),
          "displayName" : "Congressional"
        },
        "zip" : {
          "id" : "10016",
          "district" : "10016",
          "name" : "Zipcode 10016",
          "displayName" : "Zip"
        },
        "county" : {
          "id" : "974129",
          "district" : "62",
          "name" : "New York County",
          "displayName" : "County"
        },
        "town" : {
          "id" : "2395220",
          "district" : "-NYC",
          "name" : "New York City",
          "displayName" : "Town/City"
        },
        "school" : {
          "id" : "369",
          "district" : "369",
          "name" : "Manhattan SD",
          "displayName" : "School"
        },
        "electricUtility" : {
          "id" : "1002",
          "district" : "1002",
          "name" : "Consolidated Edison",
          "displayName" : "Electric Utility"
        },
        "election" : {
          "id" : "8",
          "district" : "8",
          "displayName" : "Election"
        },
        "ward" : null,
        "cityCouncil" : {
          "id" : "4",
          "district" : "4",
          "name" : "City Council District 4",
          "displayName" : "City Council"
        },
        "cleg" : null,
        "village" : null,
        "municipalCourt" : {
          "id" : "9",
          "district" : "9",
          "name" : "Municipal Court District 9",
          "displayName" : "Municipal Court"
        },
        "fire" : null
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
the district types supported by the service; a type that was not assigned is null. Each assigned district carries the
``id``, ``district``, and ``name`` fields described under :ref:`district-identifiers`. Fields that have no value are
omitted, which is why a district with no name or no member data has no ``name`` or ``member`` key at all.

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

each district portion of the response will include its geometry under ``map``. A district that has no map data
associated with it has no ``map`` key at all.

.. tip::
     ``map`` is a GeoJSON geometry: a ``type`` of ``Polygon`` or ``MultiPolygon`` and a ``coordinates`` array
     nested to match, whose innermost entries are ``[lon, lat]`` pairs.

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

The Assign_ method can handle batch requests as well. The format is::

    /api/v2/district/assign/batch

The addresses must be JSON encoded and sent along the POST payload in the same way as :ref:`batch-geocode`.
Unlike the single request version, the batch call accepts only the ``uspsValidate`` parameter, and it takes
addresses only. Coordinate pairs are not supported.

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
        "sources" : "STREETFILE, SHAPEFILE",
        "address" : {
          "addr1" : "100 Nyroy Dr",
          "addr2" : "",
          "city" : "Troy",
          "state" : "NY",
          "zip5" : "12180",
          "zip4" : "1928"
        },
        "geocode" : {
          "lat" : "42.7410467",
          "lon" : "-73.6691371",
          "quality" : "HOUSE",
          "method" : "GOOGLE",
          "cached" : true,
          "openLocCode" : "87J8P8RJ+C8"
        },
        "geocoded" : true,
        "districtAssigned" : true,
        "senateAssigned" : true,
        "uspsValidated" : true,
        "matchLevel" : "HOUSE",
        "districts" : {
          "senate" : {
            "id" : "43", "district" : "43", "name" : "Senate District 43", "displayName" : "Senate"
          },
          "assembly" : {
            "id" : "108", "district" : "108", "name" : "Assembly District 108", "displayName" : "Assembly"
          },
          "congressional" : {
            "id" : "20", "district" : "20", "name" : "Congressional District 20", "displayName" : "Congressional"
          },
          "zip" : {
            "id" : "12180", "district" : "12180", "name" : "Zipcode 12180", "displayName" : "Zip"
          },
          "county" : {
            "id" : "974140", "district" : "38", "name" : "Rensselaer County", "displayName" : "County"
          },
          "town" : {
            "id" : "979559", "district" : "-TROY", "name" : "City of Troy", "displayName" : "Town/City"
          },
          "school" : {
            "id" : "642", "district" : "642", "name" : "Troy City SD", "displayName" : "School"
          },
          "electricUtility" : {
            "id" : "1004", "district" : "1004", "name" : "National Grid", "displayName" : "Electric Utility"
          },
          "election" : {
            "id" : "12", "district" : "12", "displayName" : "Election"
          },
          "ward" : null,
          "cityCouncil" : null,
          "cleg" : {
            "id" : "1", "district" : "1", "name" : "County Legislature District 1", "displayName" : "County Legislature"
          },
          "village" : null,
          "municipalCourt" : null,
          "fire" : null
        },
        "multiMatch" : false,
        "statusCode" : 0,
        "description" : "Success."
      }, {
        "status" : "SUCCESS",
        "sources" : "STREETFILE, SHAPEFILE",
        "address" : {
          "addr1" : "44 Fairlawn Ave",
          "addr2" : "",
          "city" : "Albany",
          "state" : "NY",
          "zip5" : "12203",
          "zip4" : "1914"
        },
        "geocode" : {
          "lat" : "42.6711474",
          "lon" : "-73.79940049999999",
          "quality" : "HOUSE",
          "method" : "GOOGLE",
          "cached" : true,
          "openLocCode" : "87J8M6C2+F6"
        },
        "geocoded" : true,
        "districtAssigned" : true,
        "senateAssigned" : true,
        "uspsValidated" : true,
        "matchLevel" : "HOUSE",
        "districts" : {
          "senate" : {
            "id" : "46", "district" : "46", "name" : "Senate District 46", "displayName" : "Senate"
          },
          "assembly" : {
            "id" : "109", "district" : "109", "name" : "Assembly District 109", "displayName" : "Assembly"
          },
          "congressional" : {
            "id" : "20", "district" : "20", "name" : "Congressional District 20", "displayName" : "Congressional"
          },
          "zip" : {
            "id" : "12203", "district" : "12203", "name" : "Zipcode 12203", "displayName" : "Zip"
          },
          "county" : {
            "id" : "974099", "district" : "1", "name" : "Albany County", "displayName" : "County"
          },
          "town" : {
            "id" : "978659", "district" : "-ALBAN", "name" : "City of Albany", "displayName" : "Town/City"
          },
          "school" : {
            "id" : "5", "district" : "5", "name" : "Albany City SD", "displayName" : "School"
          },
          "electricUtility" : {
            "id" : "1004", "district" : "1004", "name" : "National Grid", "displayName" : "Electric Utility"
          },
          "election" : {
            "id" : "4", "district" : "4", "displayName" : "Election"
          },
          "ward" : {
            "id" : "13", "district" : "13", "name" : "Ward 13", "displayName" : "Ward"
          },
          "cityCouncil" : null,
          "cleg" : {
            "id" : "13", "district" : "13", "name" : "County Legislature District 13", "displayName" : "County Legislature"
          },
          "village" : null,
          "municipalCourt" : null,
          "fire" : null
        },
        "multiMatch" : false,
        "statusCode" : 0,
        "description" : "Success."
      } ],
      "status" : "SUCCESS",
      "total" : 2,
      "statusCode" : 0,
      "description" : "Success."
    }

And a parse error response in case of invalid input::

    {
      "status": "INVALID_BATCH_ADDRESSES",
      "statusCode": 55,
      "description": "The supplied batch address list could not be parsed."
    }

.. note: Batch district assignment can be configured by the application to follow a different strategy for
         purposes of improving performance. In this example the configuration was set to use street files only
         hence why many of the results are partial successes.

.. _info:

District Info
^^^^^^^^^^^^^

``info`` lists every district of a single type that SAGE knows about, along with the attributes it holds for each
one. It is the way to resolve an ``id`` returned elsewhere in the API to a name, or to populate a district picker
without hardcoding a list. It takes one required parameter:

+-------------+-------------------------------------------------------------------------------+
| Param       | Description                                                                   |
+=============+===============================================================================+
| type        | The district type to list. Required.                                          |
+-------------+-------------------------------------------------------------------------------+

The ``type`` is a district type name: ``SENATE``, ``ASSEMBLY``, ``CONGRESSIONAL``, ``ZIP``, ``COUNTY``,
``TOWN_CITY``, ``SCHOOL``, ``ELECTRIC_UTILITY``, ``ELECTION``, ``WARD``, ``CITY_COUNCIL``,
``COUNTY_LEGISLATURE``, ``VILLAGE``, ``MUNICIPAL_COURT``, or ``FIRE``::

    /api/v2/district/info?type=COUNTY

The response is a list of key/value pairs rather than an object keyed by district, because numeric keys are not
valid ``XML`` element names::

    {
      "items" : [ {
        "key" : "974099",
        "value" : {
          "gnis_id" : "974099",
          "code" : "1",
          "name" : "Albany County",
          "fips_code" : "36001",
          "swis" : "010000",
          "link" : "https://www.albanycountyny.gov/departments/health"
        }
      }, {
        "key" : "974100",
        "value" : {
          "gnis_id" : "974100",
          "code" : "2",
          "name" : "Allegany County",
          "fips_code" : "36003",
          "swis" : "020000",
          "link" : "https://www.alleganyco.gov/health-department/"
        }
      },
      ... ],
      "size" : 62
    }

Each ``key`` is a district ``id`` (see :ref:`district-identifiers`) and each ``value`` is that district's attributes,
flattened into a single object. ``size`` is the number of districts returned.

The attributes are whatever columns SAGE stores for that district type, so they vary from type to type: a county
carries its FIPS and SWIS codes and a health department link, a senate district carries little beyond its name.
Two of them are meaningful across every type - ``name``, which is always present, and ``code``, the display code
returned as ``district`` in an assignment or map response. A type whose ``id`` already is its public code has no
separate ``code`` attribute.

An election district, whose ``id`` is compound, carries the parts it was built from::

    {
      "key" : "978666-2391509-1",
      "value" : {
        "id" : "978666-2391509-1",
        "code" : "1",
        "name" : "Town of Allegany, Village of Allegany, ED 1",
        "county" : "Cattaraugus",
        "town_city_id" : "978666",
        "village_id" : "2391509",
        "ward" : null,
        "assembly_district" : null,
        "county_legislature" : null
      }
    }

Districts are ordered by ``id``: numeric ids first, in ascending order, then the rest lexicographically.

Requesting a type SAGE does not recognize returns::

    {
      "status" : "PROVIDER_NOT_SUPPORTED",
      "className" : "gov.nysenate.sage.controller.api.DistrictAssignController",
      "statusCode" : 3,
      "description" : "The requested provider is unsupported."
    }

.. caution:: Some types have no data loaded at all - ``FIRE`` currently has none - and a request for one of those
             fails rather than returning an empty list.

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
      "sources" : "STREETFILE",
      "streets" : [ {
        "bldgLoNum": 2,
        "bldgHiNum": 18,
        "street": "1ST ST",
        "location": "ALBANY",
        "zip5": "12210",
        "parity": "EVENS",
        "senate": "46",
        "congressional": "20",
        "assembly": "109",
        "town": "978659",
        "county": "974099"
      },
      {
        "bldgLoNum": 24,
        "bldgHiNum": 28,
        "street": "1ST ST",
        "location": "ALBANY",
        "zip5": "12210",
        "parity": "EVENS",
        "senate": "46",
        "congressional": "20",
        "assembly": "109",
        "town": "978659",
        "county": "974099"
      },
      ... ]
    }

Each district type is reported as its ``id`` - the town above is GNIS id ``978659``, not the ``-ALBAN`` code
that a district assignment shows. Only the types the street range actually resolves to are present.
See :ref:`district-identifiers`, and info_ for resolving these ids to names.

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
actually represent the district type to retrieve maps for. The available types are listed at ``/api/v2/map/options`` (see Options_ below).

The parameters are:

+---------------+------------------------------------------------------------------------------------+
| Params        | Description                                                                        |
+===============+====================================================================================+
| district      | Specify the district ``id``. If unspecified, all districts will be retrieved.      |
+---------------+------------------------------------------------------------------------------------+
| showMembers   | If true: senator, assembly member, and congressional member data will be appended. |
+---------------+------------------------------------------------------------------------------------+
| meta          | If true, doesn't return map geometry data.                                         |
+---------------+------------------------------------------------------------------------------------+

.. important:: ``district`` takes the district's ``id``, not its display code. The two are the same for senate,
               assembly, and congressional districts, but not for the types where they differ - New York County
               is ``district=974129``, not ``district=62``. See :ref:`district-identifiers`, and info_ for the
               ids of a given type. An id with no map loaded returns ``NO_MAP_RESULT``.

To retrieve map and member data for all senate districts::

/api/v2/map/senate?showMembers=true

To retrieve map data for just senate district 1::

/api/v2/map/senate?district=1

The response of the second query is::

    {
      "status" : "SUCCESS",
      "sources" : "SHAPEFILE",
      "map" : {
        "type" : "MultiPolygon",
        "coordinates" : (truncated)
      },
      "member" : null,
      "district" : "1",
      "name" : "Senate District 1",
      "type" : "SENATE",
      "link" : null,
      "statusCode" : 0,
      "description" : "Success."
    }

A single district's map is flattened onto the response, and reports the district by its display code in
``district``. The all-districts response instead nests one object per district under ``districts``, each carrying
the full ``id``, ``district``, and ``name`` set along with the area of the district in square kilometers::

    {
      "status" : "SUCCESS",
      "sources" : "SHAPEFILE",
      "districts" : [ {
        "id" : "974099",
        "district" : "1",
        "name" : "Albany County",
        "link" : "https://www.albanycountyny.gov/departments/health",
        "area" : 1379.9243756892906,
        "type" : "COUNTY",
        "map" : (truncated)
      },
      ... ],
      "statusCode" : 0,
      "description" : "Success."
    }

``link`` is only populated for county maps.

The member data for senate, assembly, and congressional districts will have the same senator output as in district assignment.

.. _Options:

Options
-------

The address_, geo_, district_, and map_ services each expose an ``options`` method that lists the values their
enumerated parameters accept. Rather than hardcoding provider or district type names, a client can query these
at runtime and stay in sync with the backend. Each takes no parameters::

    /api/v2/address/options
    /api/v2/geo/options
    /api/v2/district/options
    /api/v2/map/options

What each one lists:

+---------------------------+-------------------------------------------------------------------------------+
| Method                    | Lists                                                                         |
+===========================+===============================================================================+
| /api/v2/address/options   | Address validation providers, for the ``provider`` parameter.                 |
+---------------------------+-------------------------------------------------------------------------------+
| /api/v2/geo/options       | Geocoders, for the ``geocoder`` parameter.                                    |
+---------------------------+-------------------------------------------------------------------------------+
| /api/v2/district/options  | District data sources, for the ``districtSource`` parameter.                  |
+---------------------------+-------------------------------------------------------------------------------+
| /api/v2/map/options       | District types the map_ service has shapefiles loaded for. Each ``enumName``  |
|                           | may be used as the ``<method>`` segment of a map request.                     |
+---------------------------+-------------------------------------------------------------------------------+

Unlike the other methods, ``options`` returns a bare JSON array with no status wrapper. Each entry exposes the
``enumName`` (the value to supply to the API) and a human-readable ``displayName``. The response of
``/api/v2/map/options``::

    [ {
      "enumName" : "SENATE",
      "displayName" : "Senate"
    }, {
      "enumName" : "ASSEMBLY",
      "displayName" : "Assembly"
    },
    ... ]

And the response of ``/api/v2/district/options``::

    [ {
      "enumName" : "STREETFILE",
      "displayName" : "Board of Elections"
    }, {
      "enumName" : "SHAPEFILE",
      "displayName" : "LATFOR/GIS Geometry"
    } ]

.. note:: ``/api/v2/map/options`` reflects the shapefiles actually loaded in the database, so it is a subset of the
          district types SAGE knows about. The other three list every value of their enum.

Ping
----

The ``ping`` endpoint is a lightweight health check used to verify that the service is up and responding. Unlike the
other methods it is not part of the ``/api/v2`` group and does not require an API key. It takes no parameters::

    /ping

The response::

    {
      "status" : "SUCCESS",
      "description" : "Success.",
      "statusCode" : 0
    }

Status Codes
~~~~~~~~~~~~

ResultStatus.java lists all status codes. A positive response will have a status code of 0, while the rest are error statuses.

.. toctree::
   :maxdepth: 2
