package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.CityStateResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.AddressUtil;
import gov.nysenate.sage.util.UrlRequest;
import org.apache.commons.text.WordUtils;
import org.apache.http.client.fluent.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
 * It is important to note that this class is not thread-safe so the
 * calling method must ensure that multiple threads do not operate on
 * the same instance of this class.
 * Refer to the online documentation (link subject to change)
 * <a href="https://www.usps.com/webtools/_pdf/Address-Information-v3-1b.pdf">...</a>
 */
@Service
public class USPSAIS implements AddressDao {
    private static final int BATCH_SIZE = 5;
    private static final Logger logger = LoggerFactory.getLogger(USPSAIS.class);
    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    private final DocumentBuilder xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    @Value("${usps.ais.url:http://production.shippingapis.com/ShippingAPI.dll}")
    private String baseUrl;
    @Value("${usps.ais.key:API key obtained from USPS}")
    private String apiKey;

    public USPSAIS() throws ParserConfigurationException {}

    @Override
    public AddressSource source() {
        return AddressSource.AIS;
    }

    /**
     * Performs address correction and populates missing address fields.
     * @return ArrayList of AddressResult objects
     */
    @Override
    public List<AddressResult> validate(List<Address> addresses) {
        /** Short circuit invalid input */
        if (addresses == null || addresses.isEmpty()) return null;

        String url = "";
        Content page = null;
        Document response = null;

        ArrayList<AddressResult> results = new ArrayList<>();
        ArrayList<AddressResult> batchResults = new ArrayList<>();
        String xmlStartTag = "<AddressValidateRequest USERID=\""+ apiKey +"\">";
        StringBuilder xmlRequest = new StringBuilder(xmlStartTag);

        /** Start with a=1 to make the batch boundary condition work nicely */
        for (int a = 1; a <= addresses.size(); a++){
            Address address = addresses.get(a - 1);

            var addressResult = new AddressResult(source());
            addressResult.setAddress(address);
            batchResults.add(addressResult);

            xmlRequest.append(addressToXml(a - 1, address));

            /** Stop here until we've filled this batch request */
            if (a % BATCH_SIZE != 0 && a != addresses.size()) {
                continue;
            }

            xmlRequest.append("</AddressValidateRequest>");

            try
            {
                url = baseUrl +"?API=Verify&XML="+URLEncoder.encode(xmlRequest.toString(), StandardCharsets.UTF_8);
                response = xmlBuilder.parse(UrlRequest.getInputStreamFromUrl(url));

                /** If the request failed, mark them all as such */
                Node error = (Node)xpath.evaluate("Error", response, XPathConstants.NODE);
                if (error != null)
                {
                    ArrayList<String> messages = new ArrayList<>();
                    messages.add(xpath.evaluate("Description", error).trim());
                    for (AddressResult result : batchResults) {
                        result.setStatusCode(ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
                        result.setMessages(messages);
                    }
                }
                else
                {
                    NodeList responses = (NodeList)xpath.evaluate("AddressValidateResponse/Address", response, XPathConstants.NODESET);
                    for (int i = 0; i < responses.getLength(); i++) {
                        Node addressResponse = responses.item(i);
                        int index = Integer.parseInt(xpath.evaluate("@ID", addressResponse));

                        error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                        if (error != null) {
                            AddressResult result = batchResults.get(index % BATCH_SIZE);
                            result.setStatusCode(ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
                            result.addMessage(xpath.evaluate("Description", error).trim());
                            continue;
                        }

                        String addr1 = xpath.evaluate("Address1", addressResponse);
                        String addr2 = xpath.evaluate("Address2", addressResponse);
                        String city = xpath.evaluate("City", addressResponse);
                        String state = xpath.evaluate("State", addressResponse);
                        String zip5 = xpath.evaluate("Zip5", addressResponse);
                        String zip4 = xpath.evaluate("Zip4", addressResponse);
                        String returnText = xpath.evaluate("ReturnText", addressResponse);

                        /** Perform init caps on city */
                        city = (city != null) ? WordUtils.capitalizeFully(city.toLowerCase()) : addr2;

                        if (addr2 != null) {
                            /** Perform init caps on the street address */
                            addr2 = AddressUtil.initCapStreetLine(addr2);
                        }

                        /** USPS usually sets the addr2 which is not intuitive. Here we can
                         *  create a new Address object with addr1 initialized with addr2. */
                        var validatedAddr = Address.getAddress(addr2, "", city, state, zip5, zip4);

                        /** Mark address as validated */
                        validatedAddr.setUspsValidated(true);

                        if (returnText != null) {
                            batchResults.get(index % BATCH_SIZE).addMessage(returnText);
                        }

                        /** Apply to result set */
                        batchResults.get(index % BATCH_SIZE).setAddress(validatedAddr);
                    }
                }
            }
            catch (MalformedURLException e) {
                logger.error("Malformed URL '{}', check api key and address values.", url, e);
                return null;
            }
            catch (IOException e) {
                logger.error("Error opening API resource '{}'", url, e);
                return null;
            }
            catch (SAXException e) {
                logger.error("Malformed XML response for '{}'\n{}", url, page.asString(), e);
                return null;
            }
            catch (XPathExpressionException e) {
                logger.error("Unexpected XML Schema\n\n{}", response.toString(), e);
                return null;
            }
            catch (IllegalArgumentException e) {
                logger.error("Illegal argument!", e);
                return null;
            }

            xmlRequest = new StringBuilder(xmlStartTag);
            results.addAll(batchResults);
            batchResults.clear();
        }
        return results;
    }


    @Override
    public List<CityStateResult> lookupCityStates(List<Zip5> zips) {
        String url = "";
        Document response = null;

        var results = new ArrayList<CityStateResult>();
        String xmlStartTag = "<CityStateLookupRequest USERID=\""+ apiKey +"\">";
        StringBuilder xmlRequest = new StringBuilder(xmlStartTag);

        for (int i = 0; i < zips.size(); i++) {
            xmlRequest.append(String.format("<ZipCode ID=\"%s\"><Zip5>%s</Zip5></ZipCode>", i, zips.get(i).toString()));
        }

        try {
            xmlRequest.append("</CityStateLookupRequest>");
            url = baseUrl +"?API=CityStateLookup&XML=" + URLEncoder.encode(xmlRequest.toString(), StandardCharsets.UTF_8);
            response = xmlBuilder.parse(UrlRequest.getInputStreamFromUrl(url));

            /** If the request failed, mark them all as such */
            Node error = (Node)xpath.evaluate("Error", response, XPathConstants.NODE);
            if (error != null) {
                List<String> messages = new ArrayList<>();
                messages.add(xpath.evaluate("Description", error).trim());
                var result = new CityStateResult(source(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
                result.setMessages(messages);
                for (int i = 0; i < zips.size(); i++) {
                    results.add(result);
                }
                return results;
            }
            NodeList responses = (NodeList)xpath.evaluate("CityStateLookupResponse/ZipCode", response, XPathConstants.NODESET);
            for (int i = 0; i < responses.getLength(); i++) {
                Node addressResponse = responses.item(i);

                error = (Node)xpath.evaluate("Error", addressResponse, XPathConstants.NODE);
                if (error != null) {
                    var result = new CityStateResult(source(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
                    result.addMessage(xpath.evaluate("Description", error).trim());
                    results.add(result);
                    continue;
                }

                String state = xpath.evaluate("State", addressResponse);
                String city = xpath.evaluate("City", addressResponse);
                city = (city != null) ? WordUtils.capitalizeFully(city) : city;
                String zip5 = xpath.evaluate("Zip5", addressResponse);
                results.add(new CityStateResult(source(), city, state, Integer.parseInt(zip5)));
            }
        }
        catch (MalformedURLException e) {
            logger.error("Malformed URL '{}', check api key and address values.", url, e);
            return null;
        }
        catch (IOException e) {
            logger.error("Error opening API resource '{}'", url, e);
            return null;
        }
        catch (SAXException e) {
            logger.error("Malformed XML response for '{}'", url, e);
            return null;
        }
        catch (XPathExpressionException e) {
            logger.error("Unexpected XML Schema\n\n{}", response.toString(), e);
            return null;
        }

        return results;
    }

    /** The USPS API expects Address2 to contain the street address. For the request Address1 is set to
     *  be empty and Address2 contains the concatenated values of addr1 and addr2.
     * @param id    An integer id to identify responses with.
     * @param addr  The Address object to build the XML request for.
     * @return      String containing the XML request.
     */
    private static String addressToXml(int id, Address addr) {
        return String.format("<Address ID=\"%d\">"
                           + "<Address1>%s</Address1>"
                           + "<Address2>%s</Address2>"
                           + "<City>%s</City>"
                           + "<State>%s</State>"
                           + "<Zip5>%s</Zip5>"
                           + "<Zip4>%s</Zip4>"
                           + "</Address>",
                           id, "", (addr.getAddr1() + " " + addr.getAddr2()).trim(), addr.getPostalCity(),
                           addr.getState(), addr.getZip5(), addr.getZip4());
    }
}