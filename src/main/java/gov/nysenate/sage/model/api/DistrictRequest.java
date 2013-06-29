package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.service.district.DistrictServiceProvider;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;

import static gov.nysenate.sage.service.district.DistrictServiceProvider.DistrictStrategy;

public class DistrictRequest
{
    private int id;
    private int addressId;

    private ApiRequest apiRequest;
    private Address address;
    private String provider = null;
    private String geoProvider = null;
    private boolean showMembers = false;
    private boolean showMaps = false;
    private boolean uspsValidate = false;
    private boolean skipGeocode = false;
    private DistrictStrategy districtStrategy = DistrictStrategy.neighborMatch;
    private Timestamp requestTime;

    public DistrictRequest() {}

    public static DistrictRequest buildFromRequest(HttpServletRequest request) {
        return null;
    }

    public static DistrictRequest buildBluebirdRequest(ApiRequest apiRequest, Address address, String bluebirdStrategy)
    {
        DistrictRequest dr = new DistrictRequest();
        dr.setApiRequest(apiRequest);
        dr.setAddress(address);
        dr.setProvider(null);
        dr.setGeoProvider(null);
        dr.setShowMaps(false);
        dr.setShowMembers(false);
        dr.setUspsValidate(true);
        dr.setSkipGeocode(false);
        dr.setDistrictStrategy(bluebirdStrategy);
        return dr;
    }

    public DistrictRequest(ApiRequest apiRequest, Address address, String provider, String geoProvider, boolean showMembers,
                           boolean showMaps, boolean uspsValidate, boolean skipGeocode, DistrictStrategy districtStrategy)
    {
        this.apiRequest = apiRequest;
        this.address = address;
        this.provider = provider;
        this.geoProvider = geoProvider;
        this.showMembers = showMembers;
        this.showMaps = showMaps;
        this.uspsValidate = uspsValidate;
        this.skipGeocode = skipGeocode;
        this.districtStrategy = districtStrategy;
        this.requestTime = new Timestamp(new Date().getTime());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getGeoProvider() {
        return geoProvider;
    }

    public void setGeoProvider(String geoProvider) {
        this.geoProvider = geoProvider;
    }

    public boolean isShowMembers() {
        return showMembers;
    }

    public void setShowMembers(boolean showMembers) {
        this.showMembers = showMembers;
    }

    public boolean isShowMaps() {
        return showMaps;
    }

    public void setShowMaps(boolean showMaps) {
        this.showMaps = showMaps;
    }

    public boolean isUspsValidate() {
        return uspsValidate;
    }

    public void setUspsValidate(boolean uspsValidate) {
        this.uspsValidate = uspsValidate;
    }

    public boolean isSkipGeocode() {
        return skipGeocode;
    }

    public void setSkipGeocode(boolean skipGeocode) {
        this.skipGeocode = skipGeocode;
    }

    public DistrictStrategy getDistrictStrategy() {
        return districtStrategy;
    }

    public void setDistrictStrategy(DistrictStrategy districtStrategy) {
        this.districtStrategy = districtStrategy;
    }

    public void setDistrictStrategy(String districtStrategy) {
        try {
            this.districtStrategy = DistrictStrategy.valueOf(districtStrategy);
        }
        catch (Exception ex) {
            this.districtStrategy = null;
        }
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }

    public boolean hasValidAddress()
    {
        return this.address != null && !this.address.isEmpty();
    }
}
