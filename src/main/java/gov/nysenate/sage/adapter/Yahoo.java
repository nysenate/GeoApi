package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import oauth.signpost.OAuthConsumer;  
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;


public class Yahoo implements GeocodeInterface {
    private final Logger logger;
    private final XPath xpath;
    private final DocumentBuilder xmlBuilder;

    private final String CONSUMER_KEY;
    private final String CONSUMER_SECRET;
    
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
        CONSUMER_KEY = new Resource().fetch("yahoo.consumer_key");
        CONSUMER_SECRET = new Resource().fetch("yahoo.consumer_secret");
        logger.info("Initialized Yahoo Adapter");
    }

    // Yahoo doesn't implement batch geocoding so we use the single address geocoding
    // method in parallel for performance improvements on our end.    
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
        
        Document body = null;
        Result result = new Result();

        try {
            // Parse the API response
            result.source = "http://yboss.yahooapis.com/geo/placefinder?location="+URLEncoder.encode(address.as_raw(), "UTF-8").replace("+", "%20");
            logger.info(result.source);
            URL u = new URL(result.source);  
            HttpURLConnection uc = (HttpURLConnection) u.openConnection();
            OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
            consumer.sign(uc);

            if (uc.getResponseCode() != 200) {
                result.status_code = String.valueOf(uc.getResponseCode());
                result.messages.add(IOUtils.toString(uc.getErrorStream()));
                return result;
            }
            
            synchronized (xmlBuilder) {
                body = xmlBuilder.parse(uc.getInputStream());
            }

            result.status_code = xpath.evaluate("/bossresponse/@responsecode", body);
            if(!result.status_code.equals("200")) {
                // TODO: There are no examples of what an error will look like
                result.messages.add(xpath.evaluate("ResultSet/ErrorMessage", body));
                return result;
            } else {
                result.status_code = "0";
            }

            NodeList resultset = (NodeList)xpath.evaluate("/bossresponse/placefinder/results/result", body, XPathConstants.NODESET);
            for (int i=0; i < resultset.getLength(); i++) {
                Node location = resultset.item(i);
                String street = xpath.evaluate("line1", location);
                String city = xpath.evaluate("city", location);
                String state = xpath.evaluate("statecode", location);
                String zip_code = xpath.evaluate("uzip", location);
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
            String msg = "Malformed XML response for '"+result.source+"'";
            logger.error(msg, e);
            throw new GeoException(msg, e);

        } catch (XPathExpressionException e) {
            String msg = "Unexpected XML Schema\n\n"+body.toString();
            logger.error(msg, e);
            throw new GeoException(msg ,e);
        } catch (OAuthMessageSignerException e) {
            String msg = "OAuthMessageSignerException";
            logger.error(msg, e);
            throw new GeoException(msg ,e);
        } catch (OAuthExpectationFailedException e) {
            String msg = "OAuthExpectationFailedException";
            logger.error(msg, e);
            throw new GeoException(msg ,e);
        } catch (OAuthCommunicationException e) {
            String msg = "OAuthCommunicationException";
            logger.error(msg, e);
            throw new GeoException(msg ,e);
        }
    }
}
