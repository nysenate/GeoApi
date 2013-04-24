.. Sage  documentation master file, created by
   sphinx-quickstart on Fri Apr  5 15:15:48 2013.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

SAGE API v2 Reference
=================================

The SAGE API exposes methods for geocoding, address correction, and district assignment given addresses or geo-coordinates
as input. The API supports ``JSON``, ``JSON-P``, and ``XML`` output formats. There is also support for handling batch requests.

Basics
~~~~~~

The API requests must be crafted to match the following structure::

    /api/v2/<group>/<method>?params..

All API requests need to be validated using an assigned API key however it is not required when requests are made from
within the NY Senate's network. The key is supplied using the query parameter ``key``::

    /api/v2/<group>/<method>/<params..>&key=YOUR KEY HERE

The default output format is ``JSON``. To change the output to ``XML`` or ``JSON-P`` simply set the ``format`` query
parameter to ``xml`` or ``jsonp``. For example to set to xml::

    /api/v2/geo/geocode/addr=200 State St, Albany NY&format=xml

To output JSON-P a callback must also be specified.::

    /api/v2/geo/geocode/addr=200 State St, Albany NY&format=jsonp&callback=methodName

Groups
~~~~~~

Each API method belongs in a logical group in order to make it easy to understand the goal of the request. The supported
types for the ``group`` segment are as follows:

+-------------+----------------------------------------+
| Group       | Description                            |
+=============+========================================+
| address     | Address Lookup and Validation          |
+-------------+----------------------------------------+
| geo         | Geocode and Reverse Geocode            |
+-------------+----------------------------------------+
| district    | District Assignment                    |
+-------------+----------------------------------------+
| street      | Street Lookup                          |
+-------------+----------------------------------------+
| map         | Map Data                               |
+-------------+----------------------------------------+

Methods
~~~~~~~

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

The following methods are implemented for the ``address`` service:

+-------------+---------------------------------------------+
| Method      | Description                                 |
+=============+=============================================+
| validate    | Validate the given address                  |
+-------------+---------------------------------------------+
| citystate   | Lookup the city and state given the zipcode |
+-------------+---------------------------------------------+
| zipcode     | Lookup the zipcode given a street address   |
+-------------+---------------------------------------------+

The usage of ``validate`` with an address input::

    /api/v2/address/validate?addr=44 Fairlawn Avenue, Albany, NY
    /api/v2/address/validate?addr1=44 Fairlawn Avenue&city=Albany&state=NY

The validated response::

    {
      "status" : "SUCCESS",
      "source" : "USPS",
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

.. note:: USPS address validation requires addr1, city and state explicitly specified in the query parameters. Given a query that
          is missing those fields, USPS will not be used to perform validation and another provider will be used instead.

The usage of ``citystate`` with a zip code input::

    /api/v2/address/citystate?zip5=12210

The city/state response::

    {
      "status" : "SUCCESS",
      "source" : "USPS",
      "messages" : [ ],
      "city" : "ALBANY",
      "state" : "NY",
      "zip5" : "12210",
      "statusCode" : 0,
      "description" : "Success."
    }

The usage of ``zipcode``::

    /api/v2/address/zipcode?addr=44 Fairlawn Avenue, Albany, NY
    /api/v2/address/zipcode?addr1=44 Fairlawn Avenue&city=Albany&state=NY

The zipcode response::

    {
      "status" : "SUCCESS",
      "source" : "USPS",
      "messages" : [ ],
      "zip5" : "12203",
      "zip4" : "1914",
      "statusCode" : 0,
      "description" : "Success."
    }

.. note:: Zipcode lookup has the same USPS constraints as the validate method

The following providers are available for address services

* ``usps``
* ``mapquest``

To force the request to use a certain provider supply the query parameter ``provider``::

    /api/v2/address/<method>?<params..>&provider=usps
    /api/v2/address/<method>?<params..>&provider=mapquest

Geocode
-------

The following methods are implemented for the ``geo`` service:

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
| yahooboss   | Paid service from yahoo. (use sparingly)    | ?           |
+-------------+---------------------------------------------+-------------+
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
      "statusCode" : 410,
      "description" : "Geocode service returned no results."
    }

A ``RESPONSE_PARSE_ERROR`` status will also indicate a failed geocode operation.

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

District
--------

The district service has the following method(s).

+-------------+-----------------------------------------------------------------+
| Method      | Description                                                     |
+=============+=================================================================+
| assign      | Assign district information given an address or coordinate pair |
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




Contents:

.. toctree::
   :maxdepth: 2
