package gov.nysenate.sage.adapter;

import gov.nysenate.sage.model.addr.Address;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.util.Config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

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
 * USPS adapter used for performing address validations.
 *
 * The USPS Address Information API is currently only capable of sending
 * and receiving XML responses and requests. The overall format of the
 * request body is as follows:
 *
 * <XYZRequest USERID="xxxx">
 *     <Address ID="0">
 *        <FirmName></FirmName>
 *        <Address1></Address1>
 *        <Address2></Address2>
 *        <City></City>
 *        <State></State>
 *        <Zip5></Zip5>
 *        <Zip4></Zip4>
 *     </Address>
 * </XYZRequest>
 *
 * The convention for the request is that Address1 refers to the apartment
 * or suite number and Address2 refers to the street address. FirmName can
 * be thought of as the addressee line.
 *
 * In order to keep the rest of the codebase from having to deal with this
 * supply the Address model with the street address set to addr1. addr2 of
 * the supplied model if set will simply be concatenated onto addr1 of the
 * model.
 *
 * The AddressResult object that the methods return will contain a single
 * Address object. The addr1 field will contain the fully validated street
 * address. The addr2 field will always be empty. If the request failed
 * then the address object in the AddressResult will be null, isValidated
 * will be false, and the error messages will be stored in the messages array.
 *
 * It is important to note that this class is not thread-safe so the
 * calling method must ensure that multiple threads do not operate on
 * the same instance of this class.
 *
 * Refer to the online documentation (link subject to change)
 * https://www.usps.com/webtools/_pdf/Address-Information-v3-1b.pdf
 */
public class USPS implements AddressService, Observer
{
    private static final int BATCH_SIZE = 5;
    private static final String DEFAULT_BASE_URL = "http://production.shippingapis.com/ShippingAPI.dll";
    private final Logger logger = Logger.getLogger(USPS.class);
    private Config config;
    private final DocumentBuilder xmlBuilder;
    private final XPath xpath;
    private String baseUrl;
    private String apiKey;

    public USPS() throws Exception
    {
        config = ApplicationFactory.getConfig();
        configure();
        xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xpath = XPathFactory.newInstance().newXPath();
    }

    public void update(Observable o, Object arg)
    {
        configure();
    }

    /**
     * Proxies to the overloaded validate method.
     */
    @Override
    public AddressResult validate(Address address)
    {
        return validate(new ArrayList<>(Arrays.asList(address))).get(0);
    }

    /**
     *
     * @param addresses
     * @return ArrayList of AddressResult objects
     */
    @Override
    public ArrayList<AddressResult> validate(ArrayList<Address> addresses)
    {
        String url = "";
        Content page = null;
        Document response = null;

        ArrayList<AddressResult> results = new ArrayList<>();
        ArrayList<AddressResult> batchResults = new ArrayList<>();
        String xmlStartTag = "<AddressValidateRequest USERID=\""+ apiKey +"\">";
        StringBuilder xmlRequest = new StringBuilder(xmlStartTag);

        /** Start with a=1 to make the batch boundary condition work nicely */
        for (int a = 1; a <= addresses.size(); a++)
        {
            Address address = addresses.get(a - 1);

            batchResults.add(new AddressResult(this.getClass()));
            xmlRequest.append(addressToXml(a - 1, address));

            /** Stop here until we've filled this batch request */
            if (a % BATCH_SIZE != 0 && a != addresses.size()) {
                continue;
            }

            xmlRequest.append("</AddressValidateRequest>");

            try
            {
                url = baseUrl +"?API=Verify&XML="+URLEncoder.encode(xmlRequest.toString(), "UTF-8");
                logger.info(url);
                page = Request.Get(url).execute().returnContent();
                response = xmlBuilder.parse(page.asStream());

                /** If the request failed, mark them all as such */
                Node error = (Node)xpath.evaluate("Error", response, XPathConstants.NODE);
                if (error != null)
                {
                    ArrayList<String> messages = new ArrayList<String>();
                    messages.add(xpath.evaluate("Description", error));
                    messages.add("Source: "+xpath.evaluate("Source", error));
                    String status_code = xpath.evaluate("Number", error);
                    for (AddressResult result : batchResults)
                    {
                        result.setStatus(status_code);
                        result.setMessages(messages);
                    }
                }
                else
                {
                    NodeList responses = (NodeList)xpath.evaluate("AddressValidateResponse/Address", response, XPathConstants.NODESET);
                    for (int i = 0; i < responses.getLength(); i++)
                    {
                        Node addressResponse = responses.item(i);
                        error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                        if (error != null)
                        {
                            AddressResult result = batchResults.get(i);
                            result.setStatus(xpath.evaluate("Number", error));
                            result.addMessage(xpath.evaluate("Description", error));
                            result.addMessage("Source: "+xpath.evaluate("Source", error));
                            result.setValidated(false);
                            continue;
                        }

                        int index = Integer.parseInt(xpath.evaluate("@ID", addressResponse));
                        String addr1 = xpath.evaluate("Address1", addressResponse);
                        String addr2 = xpath.evaluate("Address2", addressResponse);
                        String city = xpath.evaluate("City", addressResponse);
                        String state = xpath.evaluate("State", addressResponse);
                        String zip5 = xpath.evaluate("Zip5", addressResponse);
                        String zip4 = xpath.evaluate("Zip4", addressResponse);

                        /** USPS usually sets the addr2 which is not intuitive. Here we can
                         *  create a new Address object with addr1 initialized with addr2. */
                        Address addr = new Address( addr2, "", city, state, zip5, zip4);
                        batchResults.get(index % BATCH_SIZE).setAddress(addr);
                        batchResults.get(index % BATCH_SIZE).setValidated(true);
                    }
                }
            }
            catch (MalformedURLException e) {
                logger.error("Malformed URL '"+url+"', check api key and address values.", e);
                return null;
            }
            catch (IOException e) {
                logger.error("Error opening API resource '"+url+"'", e);
                return null;
            }
            catch (SAXException e) {
                logger.error("Malformed XML response for '"+url+"'\n"+page.asString(), e);
                return null;
            }
            catch (XPathExpressionException e) {
                logger.error("Unexpected XML Schema\n\n"+response.toString(), e);
                return null;
            }

            xmlRequest = new StringBuilder(xmlStartTag);
            results.addAll(batchResults);
            batchResults.clear();
        }
        return results;
    } // validate()


    @Override
    public AddressResult lookupCityState(Address address)
    {
        return lookupCityState(new ArrayList<Address>(Arrays.asList(address))).get(0);
    } // lookupCityState()


    @Override
    public ArrayList<AddressResult> lookupCityState(ArrayList<Address> addresses)
    {
        String url = "";
        Content page = null;
        Document response = null;
        ArrayList<AddressResult> results = new ArrayList<>();
        ArrayList<AddressResult> batchResults = new ArrayList<>();
        String xmlStartTag = "<CityStateLookupRequest USERID=\""+ apiKey +"\">";
        StringBuilder xmlRequest = new StringBuilder(xmlStartTag);

        /** Start with a=1 to make the batch boundary condition work nicely */
        for (int a = 1; a <= addresses.size(); a++)
        {
            Address address = addresses.get(a-1);

            batchResults.add(new AddressResult(this.getClass()));
            xmlRequest.append(String.format("<ZipCode ID=\"%s\"><Zip5>%s</Zip5></ZipCode>", a-1, address.getZip5()));

            /** Stop here until we've filled this batch request */
            if (a%BATCH_SIZE != 0 && a != addresses.size())
            {
                continue;
            }

            try
            {
                xmlRequest.append("</CityStateLookupRequest>");
                url = baseUrl +"?API=CityStateLookup&XML="+URLEncoder.encode(xmlRequest.toString(), "UTF-8");
                logger.info(url);
                logger.debug(xmlRequest.toString());
                page = Request.Get(url).execute().returnContent();
                response = xmlBuilder.parse(page.asStream());

                /** If the request failed, mark them all as such */
                Node error = (Node)xpath.evaluate("Error", response, XPathConstants.NODE);
                if (error != null)
                {
                    ArrayList<String> messages = new ArrayList<String>();
                    messages.add(xpath.evaluate("Description", error));
                    messages.add("Source: "+xpath.evaluate("Source", error));
                    String status_code = xpath.evaluate("Number", error);
                    for (AddressResult result : batchResults)
                    {
                        result.setStatus(status_code);
                        result.setMessages(messages);
                    }
                }
                else
                {
                    NodeList responses = (NodeList)xpath.evaluate("CityStateLookupResponse/ZipCode", response, XPathConstants.NODESET);
                    for (int i = 0; i<responses.getLength(); i++)
                    {
                        Node addressResponse = responses.item(i);
                        error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                        if (error != null)
                        {
                            AddressResult result = batchResults.get(i);
                            result.setStatus(xpath.evaluate("Number", error));
                            result.addMessage(xpath.evaluate("Description", error));
                            result.addMessage("Source: "+xpath.evaluate("Source", error));
                            result.setValidated(false);
                            continue;
                        }

                        int index = Integer.parseInt(xpath.evaluate("@ID", addressResponse));
                        Address resultAddress = new Address();
                        resultAddress.setCity(xpath.evaluate("City", addressResponse));
                        resultAddress.setState(xpath.evaluate("State", addressResponse));
                        resultAddress.setZip5(xpath.evaluate("Zip5", addressResponse));

                        batchResults.get(index % BATCH_SIZE).setAddress(resultAddress);
                        batchResults.get(index % BATCH_SIZE).setValidated(true);
                    }
                }
            } catch (MalformedURLException e) {
                logger.error("Malformed URL '"+url+"', check api key and address values.", e);
                return null;

            } catch (IOException e) {
                logger.error("Error opening API resource '"+url+"'", e);
                return null;

            } catch (SAXException e) {
                logger.error("Malformed XML response for '"+url+"'\n"+page.asString(), e);
                return null;

            } catch (XPathExpressionException e) {
                logger.error("Unexpected XML Schema\n\n"+response.toString(), e);
                return null;
            }

            xmlRequest = new StringBuilder(xmlStartTag);
            results.addAll(batchResults);
            batchResults.clear();
        }
        return results;
    } // lookupCityState()


    private void configure()
    {
        baseUrl = config.getValue("usps.url");
        apiKey = config.getValue("usps.key");

        if (baseUrl.isEmpty()) {
            baseUrl = DEFAULT_BASE_URL;
        }
    } // configure()


    /** The USPS API expects Address2 to contain the street address. For the request Address1 is set to
     *  be empty and Address2 contains the concatenated values of addr1 and addr2.
     * @param id    An integer id to identify responses with.
     * @param addr  The Address object to build the XML request for.
     * @return      String containing the XML request.
     */
    private String addressToXml(int id, Address addr)
    {
        return String.format("<Address ID=\"%d\">"
                           + "<Address1>%s</Address1>"
                           + "<Address2>%s</Address2>"
                           + "<City>%s</City>"
                           + "<State>%s</State>"
                           + "<Zip5>%s</Zip5>"
                           + "<Zip4>%s</Zip4>"
                           + "</Address>",
                           id, "", (addr.getAddr1() + " " + addr.getAddr2()).trim(), addr.getCity(),
                           addr.getState(), addr.getZip5(), addr.getZip4());
    } // addressToXml()
}
