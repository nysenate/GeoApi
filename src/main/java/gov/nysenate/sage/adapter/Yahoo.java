package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    public class ParallelRequest implements Callable<Result> {
        public final Yahoo yahoo;
        public final Address address;

        ParallelRequest(Yahoo yahoo, Address address) {
            this.yahoo = yahoo;
            this.address = address;
        }

        @Override
        public Result call() throws GeoException {
            return yahoo.geocode(address);
        }
    }

    public Yahoo() throws Exception {
        logger = Logger.getLogger(this.getClass());
        xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xpath = XPathFactory.newInstance().newXPath();
        API_KEY = new Resource().fetch("yahoo.key");
        logger.info("Initialized Yahoo Adapter");
    }

    /**
     * Yahoo doesn't implement batch geocoding so we use the single address geocoding
     * method in parallel for performance improvements on our end.
    */
    @Override
    public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException {
        ArrayList<Result> results = new ArrayList<Result>();
        ExecutorService executor = Executors.newFixedThreadPool(5);
        ArrayList<Future<Result>> futureResults = new ArrayList<Future<Result>>();

        for (Address address : addresses) {
            futureResults.add(executor.submit(new ParallelRequest(this, address)));
        }

        for (Future<Result> result : futureResults) {
            try {
                results.add(result.get());
            } catch (InterruptedException e) {
                throw new GeoException(e);
            } catch (ExecutionException e) {
                throw new GeoException(e.getCause());
            }
        }
        executor.shutdown();
        return results;
    }

    @Override
    public Result geocode(Address address) throws GeoException {
        if (address==null) return null;

        Content page = null;
        Document response = null;
        Result result = new Result();

        try {
            // Parse the API response
            result.source = "http://where.yahooapis.com/geocode?appid="+API_KEY+"&q="+URLEncoder.encode(address.as_raw(), "UTF-8");
            logger.debug(result.source);
            page = Request.Get(result.source).execute().returnContent();
            synchronized (xmlBuilder) {
                response = xmlBuilder.parse(page.asStream());
            }

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

        } catch (UnsupportedEncodingException e) {
            String msg = "UTF-8 encoding not supported!?";
            logger.error(msg);
            throw new GeoException(msg);

        } catch (MalformedURLException e) {
            String msg = "Malformed URL '"+result.source+"', check api key and address values.";
            logger.error(msg, e);
            throw new GeoException(msg, e);

        } catch (IOException e) {
            String msg = "Error opening API resource '"+result.source+"'";
            logger.error(msg, e);
            result.status_code = "500";
            result.messages.add(e.getMessage());
            return result;

        } catch (SAXException e) {
            String msg = "Malformed XML response for '"+result.source+"'\n"+page.asString();
            logger.error(msg, e);
            throw new GeoException(msg, e);

        } catch (XPathExpressionException e) {
            String msg = "Unexpected XML Schema\n\n"+response.toString();
            logger.error(msg, e);
            throw new GeoException(msg ,e);
        }
    }
}