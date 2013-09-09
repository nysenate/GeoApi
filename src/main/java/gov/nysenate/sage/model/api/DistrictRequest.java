package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.job.JobProcess;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static gov.nysenate.sage.service.district.DistrictServiceProvider.DistrictStrategy;

/**
 * A DistrictRequest represents a district assignment API request.
 * It is intended to encapsulate the various options and input types.
 */
public class DistrictRequest implements Cloneable
{
    /** The ids are assigned once the request has been logged */
    private int id;
    private int addressId;

    /** Source identifiers */
    private ApiRequest apiRequest;
    private JobProcess jobProcess;

    /** User Input */
    private Address address;
    private Point point;

    /** Geocoded Input */
    private GeocodedAddress geocodedAddress;

    /** DistrictTypes to assign */
    private List<DistrictType> districtTypes = DistrictType.getStandardTypes();

    /** District assign api options */
    private String provider = null;
    private String geoProvider = null;
    private boolean showMembers = false;
    private boolean showMaps = false;
    private boolean uspsValidate = false;
    private boolean skipGeocode = false;
    private DistrictStrategy districtStrategy = DistrictStrategy.neighborMatch;
    private Timestamp requestTime;

    public DistrictRequest() {}

    /**
     * Construct a DistrictRequest to conform to certain rules for Bluebird district assign.
     * @param apiRequest The api request object
     * @param address The input address
     * @param point The input point
     * @param bluebirdStrategy The district assignment strategy for bluebird requests
     * @return DistrictRequest with preset bluebird assign options.
     */
    public static DistrictRequest buildBluebirdRequest(ApiRequest apiRequest, Address address, Point point, String bluebirdStrategy)
    {
        DistrictRequest dr = new DistrictRequest();
        dr.setApiRequest(apiRequest);
        dr.setAddress(address);
        dr.setPoint(point);
        dr.setProvider(null);
        dr.setGeoProvider(null);
        dr.setShowMaps(false);
        dr.setShowMembers(false);
        dr.setUspsValidate(true);
        dr.setSkipGeocode(false);
        dr.setDistrictStrategy(bluebirdStrategy);
        return dr;
    }

    /**
     * Modify existing DistrictRequest with bluebird options.
     * @param districtRequest DistrictRequest with options set.
     * @param bluebirdStrategy The district assignment strategy for bluebird requests.
     * @return A new DistrictRequest instance with bluebird options set.
     */
    public static DistrictRequest buildBluebirdRequest(DistrictRequest districtRequest, String bluebirdStrategy) {
        return buildBluebirdRequest(districtRequest.getApiRequest(), districtRequest.getAddress(), districtRequest.getPoint(), bluebirdStrategy);
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

    public JobProcess getJobProcess() {
        return jobProcess;
    }

    public void setJobProcess(JobProcess jobProcess) {
        this.jobProcess = jobProcess;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public GeocodedAddress getGeocodedAddress() {
        return geocodedAddress;
    }

    public void setGeocodedAddress(GeocodedAddress geocodedAddress) {
        this.geocodedAddress = geocodedAddress;
    }

    public List<DistrictType> getDistrictTypes() {
        return districtTypes;
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

    public void setDistrictTypes(List<DistrictType> districtTypes) {
        this.districtTypes = districtTypes;
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
