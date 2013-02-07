package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.AddressService.AddressInterface;
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


public class USPS implements AddressInterface, Observer
{
    private static final int BATCH_SIZE = 25;
    private static final String DEFAULT_BASE_URL = "http://production.shippingapis.com/ShippingAPI.dll";
    private final Logger logger;
    private final DocumentBuilder xmlBuilder;
    private final XPath xpath;
    private String m_baseUrl;
    private String m_apiKey;


    public USPS() throws Exception
    {
        Config.notify(this);
        configure();
        logger = Logger.getLogger(this.getClass());
        xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xpath = XPathFactory.newInstance().newXPath();
    } // USPS()


    public static void main(String[] args) throws Exception {
        USPS usps = new USPS();
        ArrayList<Address> addresses = new ArrayList<Address>(Arrays.asList(
                new Address("71 14th Street", "Troy", "NY", ""),
                new Address("214 8th Street", "Troy", "NY", "12180"),
                //new Address("39 13th Street", "Troy", "NY", "12180"),
                new Address("101 East State Street","Olean","NY","14760"),
                new Address("Oak Hill Park","Olean","NY","14760"),
                new Address("2012 E River Rd", "Olean", "NY", "14760"),
                new Address("1220 New Scottland Road", "Slingerlands", "NY", "")
            ));

        usps.validate(addresses);
    }


    public void update(Observable o, Object arg)
    {
        configure();
    } // update()


    @Override
    public Result validate(Address address)
    {
        return validate(new ArrayList<Address>(Arrays.asList(address))).get(0);
    } // validate()


    @Override
    public ArrayList<Result> validate(ArrayList<Address> addresses)
    {
        String url = "";
        Content page = null;
        Document response = null;

        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<Result> batchResults = new ArrayList<Result>();
        String xmlStartTag = "<AddressValidateRequest USERID=\""+m_apiKey+"\">";
        StringBuilder xmlRequest = new StringBuilder(xmlStartTag);

        // Start with a=1 to make the batch boundary condition work nicely
        for (int a = 1; a <= addresses.size(); a++) {
            Address address = addresses.get(a - 1);
            batchResults.add(new Result());
            xmlRequest.append(addressToXml(a - 1, address));

            // Stop here unless we've filled this batch request
            if (a%BATCH_SIZE != 0 && a != addresses.size()) {
                continue;
            }

            xmlRequest.append("</AddressValidateRequest>");

            try {
                url = m_baseUrl+"?API=Verify&XML="+URLEncoder.encode(xmlRequest.toString(), "UTF-8");
                logger.info(url);
                page = Request.Get(url).execute().returnContent();
                response = xmlBuilder.parse(page.asStream());

                // If the request failed, mark them all as such
                Node error = (Node)xpath.evaluate("Error", response, XPathConstants.NODE);
                if (error != null) {
                    ArrayList<String> messages = new ArrayList<String>();
                    messages.add(xpath.evaluate("Description", error));
                    messages.add("Source: "+xpath.evaluate("Source", error));
                    String status_code = xpath.evaluate("Number", error);
                    for (Result result : batchResults) {
                        result.setStatus(status_code);
                        result.setMessages(messages);
                        result.setSource(url);
                    }
                }
                else {
                    NodeList responses = (NodeList)xpath.evaluate("AddressValidateResponse/Address", response, XPathConstants.NODESET);
                    for (int i = 0; i < responses.getLength(); i++) {
                        Node addressResponse = responses.item(i);

                        error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                        if (error != null) {
                            Result result = batchResults.get(i);
                            result.setStatus(xpath.evaluate("Number", error));
                            result.addMessage(xpath.evaluate("Description", error));
                            result.addMessage("Source: "+xpath.evaluate("Source", error));
                            continue;
                        }

                        int index = Integer.parseInt(xpath.evaluate("@ID", addressResponse));
                        String addr1 = xpath.evaluate("Address1", addressResponse);
                        String addr2 = xpath.evaluate("Address2", addressResponse);
                        String city = xpath.evaluate("City", addressResponse);
                        String state = xpath.evaluate("State", addressResponse);
                        String zip5 = xpath.evaluate("Zip5", addressResponse);
                        String zip4 = xpath.evaluate("Zip4", addressResponse);
                        Address addr = new Address(
                                (addr1 == null) ? "" : addr1,
                                (addr2 == null) ? "" : addr2,
                                (city == null) ? "" : city,
                                (state == null) ? "" : state,
                                (zip5 == null) ? "" : zip5,
                                (zip4 == null) ? "" : zip4
                            );
                        batchResults.get(index).setAddress(addr);
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
    public Result lookupCityState(Address address)
    {
        return lookupCityState(new ArrayList<Address>(Arrays.asList(address))).get(0);
    } // lookupCityState()


    @Override
    public ArrayList<Result> lookupCityState(ArrayList<Address> addresses)
    {
        String url = "";
        Content page = null;
        Document response = null;
        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<Result> batchResults = new ArrayList<Result>();
        String xmlStartTag = "<CityStateLookupRequest USERID=\""+m_apiKey+"\">";
        StringBuilder xmlRequest = new StringBuilder(xmlStartTag);

        // Start with a=1 to make the batch boundary condition work nicely
        for (int a = 1; a <= addresses.size(); a++) {
            Address address = addresses.get(a-1);
            batchResults.add(new Result());
            xmlRequest.append(String.format("<ZipCode ID=\"%s\"><Zip5>%s</Zip5></ZipCode>", a-1, address.zip5));

            // Stop here unless we've filled this batch request
            if (a%BATCH_SIZE != 0 && a != addresses.size()) continue;

            try {
                xmlRequest.append("</CityStateLookupRequest>");
                url = m_baseUrl+"?API=CityStateLookup&XML="+URLEncoder.encode(xmlRequest.toString(), "UTF-8");
                logger.info(url);
                logger.debug(xmlRequest.toString());
                page = Request.Get(url).execute().returnContent();
                response = xmlBuilder.parse(page.asStream());

                // If the request failed, mark them all as such
                Node error = (Node)xpath.evaluate("Error", response, XPathConstants.NODE);
                if (error != null) {
                    ArrayList<String> messages = new ArrayList<String>();
                    messages.add(xpath.evaluate("Description", error));
                    messages.add("Source: "+xpath.evaluate("Source", error));
                    String status_code = xpath.evaluate("Number", error);
                    for (Result result : batchResults) {
                        result.setStatus(status_code);
                        result.setMessages(messages);
                        result.setSource(url);
                    }
                }
                else {
                    NodeList responses = (NodeList)xpath.evaluate("CityStateLookupResponse/ZipCode", response, XPathConstants.NODESET);
                    for (int i=0; i<responses.getLength(); i++) {
                        Node addressResponse = responses.item(i);

                        error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                        if (error != null) {
                            Result result = batchResults.get(i);
                            result.setStatus(xpath.evaluate("Number", error));
                            result.addMessage(xpath.evaluate("Description", error));
                            result.addMessage("Source: "+xpath.evaluate("Source", error));
                            continue;
                        }

                        int index = Integer.parseInt(xpath.evaluate("@ID", addressResponse));
                        Address resultAddress = new Address();
                        resultAddress.city = xpath.evaluate("City", addressResponse);
                        resultAddress.state = xpath.evaluate("State", addressResponse);
                        resultAddress.zip5 = xpath.evaluate("Zip5", addressResponse);
                        batchResults.get(index).setAddress(resultAddress);
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
        m_baseUrl = Config.read("usps.url");
        m_apiKey = Config.read("usps.key");

        if (m_baseUrl.isEmpty()) {
            m_baseUrl = DEFAULT_BASE_URL;
        }
    } // configure()


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
                           id, addr.addr1, addr.addr2, addr.city,
                           addr.state, addr.zip5, addr.zip4);
    } // addressToXml()
}
