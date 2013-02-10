package gov.nysenate.sage;

import gov.nysenate.sage.service.DistrictService;
import java.util.ArrayList;

public class DistrictResult {

    protected Address address;
    protected ArrayList<DistrictService.TYPE> unassignedDistricts;

    protected ArrayList<Address> addresses;
    public ArrayList<String> messages = new ArrayList<String>();

    protected int statusCode;
    protected String source;

    public DistrictResult() {}

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public void setStatusCode(int statusCode){
        this.statusCode = statusCode;
    }

    public ArrayList<DistrictService.TYPE> getUnassignedDistricts() {
        return unassignedDistricts;
    }

    public void setUnassignedDistricts(ArrayList<DistrictService.TYPE> missingDistricts) {
        this.unassignedDistricts = missingDistricts;
    }

    public ArrayList<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(ArrayList<Address> addresses) {
        this.addresses = addresses;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<String> messages) {
        this.messages = messages;
    }
}
