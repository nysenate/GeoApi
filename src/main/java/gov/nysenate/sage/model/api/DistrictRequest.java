package gov.nysenate.sage.model.api;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.TimeUtil;

import java.sql.Timestamp;
import java.util.List;

import static gov.nysenate.sage.service.district.DistrictServiceProvider.DistrictStrategy;

/**
 * A DistrictRequest represents a district assignment API request.
 * It is intended to encapsulate the various options and input types.
 */
public abstract class DistrictRequest {
    /** The ids are assigned once the request has been logged */
    private int id;
    private JobProcess jobProcess;

    /** Geocoded Input */
    private GeocodedAddress geocodedAddress;

    /** DistrictTypes to assign */
    private List<DistrictType> districtTypes = DistrictType.getStandardTypes();

    /** District assign api options */
    protected DistrictSource provider = null;
    protected Geocoder geoProvider = null;
    protected boolean uspsValidate = false;
    protected boolean usePunct = false;
    protected boolean skipGeocode = false;
    protected DistrictStrategy districtStrategy = DistrictStrategy.neighborMatch;
    private final Timestamp requestTime = TimeUtil.currentTimestamp();

    public DistrictRequest() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JobProcess getJobProcess() {
        return jobProcess;
    }

    public void setJobProcess(JobProcess jobProcess) {
        this.jobProcess = jobProcess;
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

    public DistrictSource getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        if (provider != null) {
            this.provider = DistrictSource.valueOf(FormatUtil.cleanString(provider.toUpperCase()));
        }
    }

    public Geocoder getGeoProvider() {
        return geoProvider;
    }

    public void setGeoProvider(String geoProvider) {
        if (geoProvider != null) {
            this.geoProvider = Geocoder.valueOf(FormatUtil.cleanString(geoProvider));
        }
    }

    public boolean isUspsValidate() {
        return uspsValidate;
    }

    public void setUspsValidate(boolean uspsValidate) {
        this.uspsValidate = uspsValidate;
    }

    public boolean isUsePunct() {
        return usePunct;
    }

    public void setUsePunct(boolean usePunct) {
        this.usePunct = usePunct;
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
            this.districtStrategy = DistrictStrategy.valueOf(FormatUtil.cleanString(districtStrategy));
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
}
