package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author graylin
 *
 *
 */
public class MapQuest implements GeocodeInterface{
    private final Logger logger;
    private final DocumentBuilder xmlBuilder;
    private final XPath xpath;
    private final String BASE_URL;
    private final int BATCH_SIZE = 95;
    private final HashMap<String, Integer> qualityMap;

    public MapQuest() throws Exception {
        logger = Logger.getLogger(this.getClass());
        xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xpath = XPathFactory.newInstance().newXPath();

        /* Point Values Based on Yahoo's quality point scheme
         *   http://developer.yahoo.com/geo/placefinder/guide/responses.html#address-quality
         *
         * and the MapQuest geocode quality codes
         *   http://www.mapquestapi.com/geocoding/geocodequality.html

            P1  POINT   A specific point location.
            L1  ADDRESS A specific street address location.
            I1  INTERSECTION    An intersection of two or more streets.
            B1  STREET  The center of a single street block. House number ranges are returned if available.
            B2  STREET  The center of a single street block, which is located closest to the geographic center of all matching street blocks. No house number range is returned.
            B3  STREET  The center of a single street block whose numbered range is nearest to the input number. House number range is returned.
            A1  COUNTRY Admin area, largest. For USA, a country.
            A3  STATE   Admin area. For USA, a state.
            A4  COUNTY  Admin area. For USA, a county.
            A5  CITY    Admin area. For USA, a city.
            Z1  ZIP Postal code, largest. For USA, a ZIP.
            Z2  ZIP_EXTENDED    Postal code. For USA, a ZIP+2.
            Z3  ZIP_EXTENDED    Postal code. For USA, a ZIP+4.
            Z4  ZIP Postal code, smallest. Unused in USA.
         */
        qualityMap = new HashMap<String, Integer>();
        qualityMap.put("P1", 99);
        qualityMap.put("L1", 87);
        qualityMap.put("I1", 82);
        qualityMap.put("B1", 72);
        qualityMap.put("B2", 72);
        qualityMap.put("B3", 72);
        qualityMap.put("A1", 10);
        qualityMap.put("A3", 20);
        qualityMap.put("A4", 30);
        qualityMap.put("A5", 40);
        qualityMap.put("Z1", 60);
        qualityMap.put("Z2", 64);
        qualityMap.put("Z3", 75);

        // Show only one result per location
        // Use XML output
        // Don't bother with the map thumbnail images
        BASE_URL ="http://www.mapquestapi.com/geocoding/v1/batch?key="+Config.read("mapquest.key")+"&outFormat=xml&thumbMaps=false&maxResults=1";
        logger.info("Initialized MapQuest Adapter");
    }

    @Override
    public Result geocode(Address address) throws GeoException {
        // Always use bulk with mapquest
        return geocode(new ArrayList<Address>(Arrays.asList(address)), Address.TYPE.MIXED).get(0);
    }

    @Override
    public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException {
        Content page = null;
        Document response = null;
        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<Result> batchResults = new ArrayList<Result>();

        String url = BASE_URL;

        try {
            // Start with a=1 to make the batch boundary condition work nicely
            for (int a=1; a <= addresses.size(); a++) {
                Address address = addresses.get(a-1);
                if (address == null) {
                    batchResults.add(null);
                } else {
                    url += "&location="+URLEncoder.encode(address.as_raw(), "UTF-8");
                    batchResults.add(new Result());
                }

                // Stop here unless we've filled this batch request
                if (a%BATCH_SIZE != 0 && a != addresses.size()) continue;

                // Parse the API response
                logger.info(url);
                page = Request.Get(url).execute().returnContent();
                response = xmlBuilder.parse(page.asStream());

                // Log the url and status code in all results
                String status = xpath.evaluate("response/info/statusCode", response);
                for (Result result : batchResults) {
                    if (result != null) {
                        result.status_code = status;
                        result.source = url;
                    }
                }

                // Check for an error code
                // TODO: 607 - You have exceeded your daily limit of transactions
                if (!status.equals("0")) {
                    ArrayList<String> resultMessages = new ArrayList<String>();
                    NodeList messages = (NodeList)xpath.evaluate("response/info/messages/message", response, XPathConstants.NODESET);
                    for (int i=0; i < messages.getLength(); i++) {
                        resultMessages.add(messages.item(i).getTextContent());
                    }

                    // Log the error messages in each of the results.
                    for (Result result : batchResults) {
                        if (result != null) {
                            result.messages = resultMessages;
                        }
                    }


                } else {

                    // Each address specified produces its own result node
                    // Because null addresses aren't sent to mapquest we need
                    // to track an offset to the corresponding result.
                    int resultOffset = 0;
                    NodeList responses = (NodeList)xpath.evaluate("response/results/result", response, XPathConstants.NODESET);
                    for (int i = 0; i < responses.getLength(); i++) {
                        Node responseItem = responses.item(i);
                        Result result;
                        while ((result = batchResults.get(i+resultOffset)) == null) {
                            resultOffset++;
                        }

                        // Store all the locations passed back by mapquest
                        NodeList locations = (NodeList)xpath.evaluate("locations/location", responseItem, XPathConstants.NODESET);
                        for (int l=0; l < locations.getLength(); l++) {
                            Node location = locations.item(l);

                            String street = xpath.evaluate("street", location);
                            String city = xpath.evaluate("adminArea5", location);
                            String state = xpath.evaluate("adminArea3", location);
                            String zip_code = xpath.evaluate("postalCode", location);

                            String qualityCode = xpath.evaluate("geocodeQualityCode", location);
                            int quality = qualityMap.get(qualityCode.substring(0, 2));
                            double lat = (Double)xpath.evaluate("latLng/lat", location, XPathConstants.NUMBER);
                            double lng = (Double)xpath.evaluate("latLng/lng", location, XPathConstants.NUMBER);

                            Address resultAddress = new Address(street, city, state, zip_code);
                            resultAddress.setGeocode(lat, lng, quality);
                            result.addresses.add(resultAddress);
                        }
                        result.address = result.addresses.get(0);
                    }
                }
                url = BASE_URL;
                results.addAll(batchResults);
                batchResults.clear();
            }
            return results;

        } catch (UnsupportedEncodingException e) {
            String msg = "UTF-8 encoding not supported!?";
            logger.error(msg);
            throw new GeoException(msg);

        } catch (MalformedURLException e) {
            String msg = "Malformed URL '"+url+"', check api key and address values.";
            logger.error(msg, e);
            throw new GeoException(msg, e);

        } catch (IOException e) {
            String msg = "Error opening API resource '"+url+"'";
            logger.error(msg, e);
            throw new GeoException(msg ,e);

        } catch (SAXException e) {
            String msg = "Malformed XML response for '"+url+"'\n"+page.asString();
            logger.error(msg, e);
            throw new GeoException(msg, e);

        } catch (XPathExpressionException e) {
            String msg = "Unexpected XML Schema\n\n"+response.toString();
            logger.error(msg, e);
            throw new GeoException(msg ,e);
        }
    }
}
