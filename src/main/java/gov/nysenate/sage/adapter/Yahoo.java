package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;

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

public class Yahoo implements GeocodeInterface {
    private final Logger logger;
    private final XPath xpath;
    private final DocumentBuilder xmlBuilder;

    private final String API_KEY;

    public Yahoo() throws Exception {
        logger = Logger.getLogger(this.getClass());
        xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xpath = XPathFactory.newInstance().newXPath();
        API_KEY = new Resource().fetch("yahoo.key");
        logger.info("Initialized Yahoo Adapter");
    }

    /**
     * Yahoo doesn't implement batch geocoding so we just wrap a bunch of calls here.
     *
     * @throws UnsupportedEncodingException
     */
    @Override
    public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) {
        return geocode(addresses); // Yahoo doesn't need the type hint, only raw supported
    }

    public ArrayList<Result> geocode(ArrayList<Address> addresses) {
        ArrayList<Result> results = new ArrayList<Result>();
        for (Address address : addresses) {
            results.add(geocode(address));
        }
        return results;
    }

    @Override
    public Result geocode(Address address) {
        Content page = null;
        Document response = null;
        Result result = new Result();

        try {
            // Parse the API response
            result.source = "http://where.yahooapis.com/geocode?appid="+API_KEY+"&q="+URLEncoder.encode(address.as_raw(), "UTF-8");
            logger.info(result.source);
            page = Request.Get(result.source).execute().returnContent();
            response = xmlBuilder.parse(page.asStream());

            result.status_code = xpath.evaluate("ResultSet/Error", response);
            if(!result.status_code.equals("0")) {
                result.messages.add(xpath.evaluate("ResultSet/ErrorMessage", response));
                return result;
            }

            NodeList resultset = (NodeList)xpath.evaluate("ResultSet/Result", response, XPathConstants.NODESET);
            for (int i=0; i < resultset.getLength(); i++) {
                Node location = resultset.item(i);
                String street = xpath.evaluate("line1", location);
                String city = xpath.evaluate("city", location);
                String state = xpath.evaluate("statecode", location);
                String zip_code = xpath.evaluate("postal", location);
                int quality = Integer.valueOf(xpath.evaluate("quality", location));
                double lat = (Double)xpath.evaluate("offsetlat", location, XPathConstants.NUMBER);
                double lng = (Double)xpath.evaluate("offsetlon", location, XPathConstants.NUMBER);

                Address resultAddress = new Address(street, city, state, zip_code);
                resultAddress.setGeocode(lat, lng, quality);
                result.addresses.add(resultAddress);
            }

            return result;

        } catch (MalformedURLException e) {
            logger.error("Malformed URL '"+result.source+"', check api key and address values.", e);
            return result;

        } catch (IOException e) {
            logger.error("Error opening API resource '"+result.source+"'", e);
            return result;

        } catch (SAXException e) {
            logger.error("Malformed XML response for '"+result.source+"'\n"+page.asString(), e);
            return result;

        } catch (XPathExpressionException e) {
            logger.error("Unexpected XML Schema\n\n"+response.toString(), e);
            return result;
        }
    }
}