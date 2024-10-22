package gov.nysenate.sage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("environment")
public class Environment {

    @Value("${base.url:http://localhost:8080}") private String baseUrl;

    @Value("${mass.geocache.error.log}") private String massGeocacheError;

    @Value("${user.ip.filter:(127.0.0.1)}") private String userIpFilter;

    @Value("${user.default.key}") private String userDefaultKey;

    @Value("${api.logging.enabled:true}") private boolean apiLoggingEnabled;

    @Value("${detailed.logging.enabled:true}") private boolean detailedLoggingEnabled;

    @Value("${batch.detailed.logging.enabled:false}") private boolean batchDetailedLoggingEnabled;

    @Value("${usps.ams.api.url:http://localhost:8081/USPS-AMS/api/}") private String uspsAmsApiUrl;

    @Value("${usps.ams.ui.url:http://localhost:8081/USPS-AMS/}") private String uspsAmsUiUrl;

    @Value("${nysenate.domain:http://www.nysenate.gov}") private String nysenateDomain;

    @Value("${google.maps.url:https://maps.google.com/maps/api/js?v=3&libraries=places}") private String googleMapsUrl;

    @Value("${google.maps.key:API Key obtained from Google (this key is public facing)}") private String googleMapsKey;

    @Value("${geocoder.active}") private String geocoderActive;

    @Value("${geocoder.rank}") private String geocoderRank;

    @Value("${validate.threads:3}") private int validateThreads;

    @Value("${geoserver.url:http://geoserver:8080/wfs}") private String geoServerUrl;

    @Value("${geoserver.workspace:nysenate}") private String geoserverWorkspace;

    @Value("${job.upload.dir:/data/geoapi_data/uploads/}") private String jobUploadDir;

    @Value("${job.download.dir:/data/geoapi_data/downloads/}") private String jobDownloadDir;

    @Value("${env.districts.schema:districts}") private String districtsSchema;

    @Value("${env.job.schema:job}") private String jobSchema;

    @Value("${env.log.schema:log}") private String logSchema;

    @Value("${env.public.schema:public}") private String publicSchema;

    @Value("${env.cache.schema:cache}") private String cacheSchema;

    public String getBaseUrl() {
        return baseUrl.trim();
    }

    public String getUserIpFilter() {
        return userIpFilter.trim();
    }

    public String getUserDefaultKey() {
        return userDefaultKey.trim();
    }

    public boolean isApiLoggingEnabled() {
        return apiLoggingEnabled;
    }

    public boolean isDetailedLoggingEnabled() {
        return detailedLoggingEnabled;
    }

    public boolean isBatchDetailedLoggingEnabled() {
        return batchDetailedLoggingEnabled;
    }

    public String getUspsAmsApiUrl() {
        return uspsAmsApiUrl.trim();
    }

    public String getUspsAmsUiUrl() {
        return uspsAmsUiUrl.trim();
    }

    public String getNysenateDomain() {
        return nysenateDomain.trim();
    }

    public String getGeocoderActive() {
        return geocoderActive.trim();
    }

    public String getGeocoderRank() {
        return geocoderRank.trim();
    }

    public int getValidateThreads() {
        return validateThreads;
    }

    public String getGeoServerUrl() {
        return geoServerUrl.trim();
    }

    public String getGeoserverWorkspace() {
        return geoserverWorkspace.trim();
    }

    public String getJobUploadDir() {
        return jobUploadDir.trim();
    }

    public String getJobDownloadDir() {
        return jobDownloadDir.trim();
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl.trim();
    }

    public String getGoogleMapsKey() {
        return googleMapsKey.trim();
    }

    public String getDistrictsSchema() {
        return districtsSchema.trim();
    }

    public String getJobSchema() {
        return jobSchema.trim();
    }

    public String getLogSchema() {
        return logSchema.trim();
    }

    public String getPublicSchema() {
        return publicSchema.trim();
    }

    public String getCacheSchema() {
        return cacheSchema.trim();
    }

    // TODO: more to move
    public String getMassGeocacheErrorLog() {
        return massGeocacheError;
    }
}
