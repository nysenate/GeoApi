package gov.nysenate.sage.adapter;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.AddressService.AddressInterface;
import gov.nysenate.sage.util.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

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

public class USPS implements AddressInterface {
    public static final int BATCH_SIZE = 25;
    public final String API_ID;
    public final String API_BASE = "http://production.shippingapis.com/ShippingAPI.dll";

    private final Logger logger;
    private final DocumentBuilder xmlBuilder;
    private final XPath xpath;

    public USPS() throws Exception {
        API_ID = new Resource().fetch("usps.key");
        logger = Logger.getLogger(this.getClass());
        xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        xpath = XPathFactory.newInstance().newXPath();
    }

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

    @Override
    public Result validate(Address address) {
        return validate(new ArrayList<Address>(Arrays.asList(address))).get(0);
    }

    @Override
    public ArrayList<Result> validate(ArrayList<Address> addresses) {
        String url = "";
        Content page = null;
        Document response = null;

        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<Result> batchResults = new ArrayList<Result>();
        String request = "<AddressValidateRequest USERID=\""+API_ID+"\">";

        // Start with a=1 to make the batch boundary condition work nicely
        for (int a=1; a <= addresses.size(); a++) {
            Address address = addresses.get(a-1);
            batchResults.add(new Result());
            request += String.format(
                "<Address ID=\"%s\">"
                    + "<Address1>%s</Address1>"
                    + "<Address2>%s</Address2>"
                    + "<City>%s</City>"
                    + "<State>%s</State>"
                    + "<Zip5>%s</Zip5>"
                    + "<Zip4>%s</Zip4>"
                + "</Address>", a-1, address.addr1, address.addr2, address.city, address.state, address.zip5, address.zip4);

            // Stop here unless we've filled this batch request
            if (a%BATCH_SIZE != 0 && a != addresses.size()) continue;

            try {
                request += "</AddressValidateRequest>";
                url = API_BASE+"?API=Verify&XML="+URLEncoder.encode(request, "UTF-8");
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
                        result.status_code = status_code;
                        result.messages = messages;
                        result.source = url;
                    }

                } else {
                    NodeList responses = (NodeList)xpath.evaluate("AddressValidateResponse/Address", response, XPathConstants.NODESET);
                    for (int i=0; i<responses.getLength(); i++) {
                        Node addressResponse = responses.item(i);

                        error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                        if (error != null) {
                            Result result = batchResults.get(i);
                            result.status_code = xpath.evaluate("Number", error);
                            result.messages.add(xpath.evaluate("Description", error));
                            result.messages.add("Source: "+xpath.evaluate("Source", error));
                            continue;
                        }

                        int index = Integer.parseInt(xpath.evaluate("@ID", addressResponse));
                        String addr1 = xpath.evaluate("Address1", addressResponse);
                        String addr2 = xpath.evaluate("Address2", addressResponse);
                        String city = xpath.evaluate("City", addressResponse);
                        String state = xpath.evaluate("State", addressResponse);
                        String zip5 = xpath.evaluate("Zip5", addressResponse);
                        String zip4 = xpath.evaluate("Zip4", addressResponse);
                        batchResults.get(index).address = new Address(
                                (addr1 == null) ? "" : addr1,
                                (addr2 == null) ? "" : addr2,
                                (city == null) ? "" : city,
                                (state == null) ? "" : state,
                                (zip5 == null) ? "" : zip5,
                                (zip4 == null) ? "" : zip4
                            );
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

            request = "<AddressValidateRequest USERID=\""+API_ID+"\">";
            results.addAll(batchResults);
            batchResults.clear();
        }
        return results;
    }

    @Override
    public Result lookupCityState(Address address) {
        return lookupCityState(new ArrayList<Address>(Arrays.asList(address))).get(0);
    }

    @Override
    public ArrayList<Result> lookupCityState(ArrayList<Address> addresses) {
        String url = "";
        Content page = null;
        Document response = null;
        ArrayList<Result> results = new ArrayList<Result>();
        ArrayList<Result> batchResults = new ArrayList<Result>();
        String request = "<CityStateLookupRequest USERID=\""+API_ID+"\">";

        // Start with a=1 to make the batch boundary condition work nicely
        for (int a=1; a <= addresses.size(); a++) {
            Address address = addresses.get(a-1);
            batchResults.add(new Result());
            request += String.format("<Address ID=\"%s\"><Zip5>%s</Zip5></Address>", a-1, address.zip5);

            // Stop here unless we've filled this batch request
            if (a%BATCH_SIZE != 0 && a != addresses.size()) continue;

            try {
                request += "</CityStateLookupRequest>";
                url = API_BASE+"?API=CityStateLookup&XML="+URLEncoder.encode(request, "UTF-8");
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
                        result.status_code = status_code;
                        result.messages = messages;
                        result.source = url;
                    }

                } else {
                    NodeList responses = (NodeList)xpath.evaluate("AddressValidateResponse/Address", response, XPathConstants.NODESET);
                    for (int i=0; i<responses.getLength(); i++) {
                        Node addressResponse = responses.item(i);

                        error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                        if (error != null) {
                            Result result = batchResults.get(i);
                            result.status_code = xpath.evaluate("Number", error);
                            result.messages.add(xpath.evaluate("Description", error));
                            result.messages.add("Source: "+xpath.evaluate("Source", error));
                            continue;
                        }

                        int index = Integer.parseInt(xpath.evaluate("@ID", addressResponse));
                        Address resultAddress = new Address();
                        resultAddress.city = xpath.evaluate("City", addressResponse);
                        resultAddress.state = xpath.evaluate("State", addressResponse);
                        resultAddress.zip5 = xpath.evaluate("Zip5", addressResponse);
                        batchResults.get(index).address = resultAddress;
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

            request = "<AddressValidateRequest USERID=\""+API_ID+"\">";
            results.addAll(batchResults);
            batchResults.clear();
        }
        return results;
    }
}