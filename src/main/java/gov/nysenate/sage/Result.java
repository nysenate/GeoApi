package gov.nysenate.sage;

import java.util.ArrayList;
/**
 *
 * @author Graylin Kim
 *
 * A generic Result structure passed back from all services.
 *
 * Each service translates the 3rd party response into a response object which
 * can be uniformly handled and provides certain consistencies:
 *
 *      1. If status_code = "0" the request was successful
 *      2. If status_code is non-zero then messages propagate any 3rd party response messages.
 *      3. source is the fully encoded url associated with the request
 *      4. Services will either fill addresses or address . See service docs for details.
 *
 * This result structure allows a service which does bulk operations to consistently
 * return an array of results even in the face of partial failure while retaining all
 * relevant error details for further action by the caller.
 */
public class Result {
    public Address address;
    public ArrayList<Address> addresses = new ArrayList<Address>();
    public ArrayList<String> messages = new ArrayList<String>();
    public String status_code = "0";
    public String source = "";

    public Result() {}

    public Result(Address address, ArrayList<String> messages) {
        this.address = address;
        this.messages = messages;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
