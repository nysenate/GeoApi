package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.service.GeoService.GeocodeInterface;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
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

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class YahooBoss implements GeocodeInterface, Observer
{
  private static final String DEFAULT_BASE_URL = "http://yboss.yahooapis.com/geo/placefinder";
  private final Logger logger;
  private final XPath xpath;
  private final DocumentBuilder xmlBuilder;

  private String m_baseUrl;
  private String m_consumerKey;
  private String m_consumerSecret;


  public class ParallelRequest implements Callable<Result>
  {
    public final YahooBoss yahooBoss;
    public final Address address;

    ParallelRequest(YahooBoss yahooBoss, Address address)
    {
      this.yahooBoss = yahooBoss;
      this.address = address;
    }

    @Override
    public Result call() throws GeoException
    {
      return yahooBoss.geocode(address);
    }
  } // ParallelRequest


  public YahooBoss() throws Exception
  {
    Config.notify(this);
    configure();
    logger = Logger.getLogger(this.getClass());
    xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    xpath = XPathFactory.newInstance().newXPath();
    logger.info("Initialized Yahoo Adapter");
  }

  public void update(Observable o, Object arg)
  {
    configure();
  } // update()


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
  public Result geocode(Address address) throws GeoException
  {
    Document body = null;
    Result result = null;

    if (address == null) {
      return result;
    }

    try {
      // Parse the API response
      String urlText = "http://yboss.yahooapis.com/geo/placefinder?location="+URLEncoder.encode(address.as_raw(), "UTF-8").replace("+", "%20");
      result = new Result(urlText);
      logger.info(urlText);

      URL u = new URL(urlText);
      HttpURLConnection uc = (HttpURLConnection)u.openConnection();
      OAuthConsumer consumer = new DefaultOAuthConsumer(m_consumerKey, m_consumerSecret);
      consumer.sign(uc);

      if (uc.getResponseCode() != HttpURLConnection.HTTP_OK) {
        result.setStatus(String.valueOf(uc.getResponseCode()));
        result.addMessage(IOUtils.toString(uc.getErrorStream()));
        return result;
      }

      synchronized (xmlBuilder) {
        body = xmlBuilder.parse(uc.getInputStream());
      }

      String status = xpath.evaluate("/bossresponse/@responsecode", body);
      if (!status.equals("200")) {
        result.setStatus(status);
        // TODO: There are no examples of what an error will look like
        result.addMessage(xpath.evaluate("ResultSet/ErrorMessage", body));
        return result;
      }

      NodeList resultset = (NodeList)xpath.evaluate("/bossresponse/placefinder/results/result", body, XPathConstants.NODESET);
      for (int i = 0; i < resultset.getLength(); i++) {
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
        result.addAddress(resultAddress);
      }

      return result;
    }
    catch (UnsupportedEncodingException e) {
      String msg = "UTF-8 encoding not supported!?";
      logger.error(msg);
      throw new GeoException(msg);
    }
    catch (MalformedURLException e) {
      String msg = "Malformed URL '"+result.getSource()+"', check API key and address values.";
      logger.error(msg, e);
      throw new GeoException(msg, e);
    }
    catch (IOException e) {
      String msg = "Error opening API resource '"+result.getSource()+"'";
      logger.error(msg, e);
      result.setStatus("500");
      result.addMessage(e.getMessage());
      return result;
    }
    catch (SAXException e) {
      String msg = "Malformed XML response for '"+result.getSource()+"'";
      logger.error(msg, e);
      throw new GeoException(msg, e);
    }
    catch (XPathExpressionException e) {
      String msg = "Unexpected XML Schema\n\n"+body.toString();
      logger.error(msg, e);
      throw new GeoException(msg ,e);
    }
    catch (OAuthMessageSignerException e) {
      String msg = "OAuthMessageSignerException";
      logger.error(msg, e);
      throw new GeoException(msg ,e);
    }
    catch (OAuthExpectationFailedException e) {
      String msg = "OAuthExpectationFailedException";
      logger.error(msg, e);
      throw new GeoException(msg ,e);
    }
    catch (OAuthCommunicationException e) {
      String msg = "OAuthCommunicationException";
      logger.error(msg, e);
      throw new GeoException(msg ,e);
    }
  }


  private void configure() {
    m_baseUrl = Config.read("yahoo.boss.url");
    m_consumerKey = Config.read("yahoo.boss.consumer_key");
    m_consumerSecret = Config.read("yahoo.boss.consumer_secret");

    if (m_baseUrl.isEmpty()) {
      m_baseUrl = DEFAULT_BASE_URL;
    }
  } //configure()
}
