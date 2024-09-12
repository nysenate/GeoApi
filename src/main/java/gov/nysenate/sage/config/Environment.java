package gov.nysenate.sage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("environment")
public class Environment {

    @Value("${base.url:http://localhost:8080}") private String baseUrl;

    @Value("${senator.cache.refresh.hours:12}") private int senatorCacheRefreshHours;

    @Value("${mass.geocache.error.log}") private String massGeocacheError;

    @Value("${user.ip.filter:(127.0.0.1)}") private String userIpFilter;

    @Value("${user.default.key}") private String userDefaultKey;

    @Value("${public.api.filter:(map)}") private String publicApiFilter;

    @Value("${user.public.key}") private String userPublicKey;

    @Value("${api.logging.enabled:true}") private boolean apiLoggingEnabled;

    @Value("${detailed.logging.enabled:true}") private boolean detailedLoggingEnabled;

    @Value("${batch.detailed.logging.enabled:false}") private boolean batchDetailedLoggingEnabled;

    @Value("${usps.ais.url:http://production.shippingapis.com/ShippingAPI.dll}") private String uspsAisUrl;

    @Value("${usps.ais.key:API key obtained from USPS}") private String uspsAisKey;

    @Value("${usps.ams.api.url:http://localhost:8081/USPS-AMS/api/}") private String uspsAmsApiUrl;

    @Value("${usps.ams.ui.url:http://localhost:8081/USPS-AMS/}") private String uspsAmsUiUrl;

    @Value("${nysenate.domain:http://www.nysenate.gov}") private String nysenateDomain;

    @Value("${google.geocoder.url:https://maps.googleapis.com/maps/api/geocode/json}") private String googleGeocoderUrl;

    @Value("${google.geocoder.key:API Key obtained from Google}") private String googleGeocoderKey;

    @Value("${google.maps.url:https://maps.google.com/maps/api/js?v=3&libraries=places}") private String googleMapsUrl;

    @Value("${google.maps.key:API Key obtained from Google (this key is public facing)}") private String googleMapsKey;

    @Value("${usps.default:usps}") private String uspsDefault;

    @Value("${geocoder.active}") private String geocoderActive;

    @Value("${geocoder.rank}") private String geocoderRank;

    @Value("${geocoder.cacheable}") private String geocoderCacheable;

    @Value("${geocoder.retry.interval:300}") private int geocoderRetryInterval;

    @Value("${validate.threads:3}") private int validateThreads;

    @Value("${geoserver.url:http://geoserver:8080/wfs}") private String geoServerUrl;

    @Value("${geoserver.workspace:nysenate}") private String geoserverWorkspace;

    @Value("${smtp.active}") private Boolean smtpActive;

    @Value("${smtp.host}") private String smtpHost;

    @Value("${smtp.port}") private int smtpPort;

    @Value("${smtp.user}") private String smtpUser;

    @Value("${smtp.pass}") private String smtpPass;

    @Value("${smtp.debug}") private Boolean smtpDebug;

    @Value("${smtp.admin}") private String smtpAdmin;

    @Value("${smtp.tls.enable}") private Boolean smtpTlsEnable;

    @Value("${smtp.ssl.enable}") private Boolean smtpSslEnable;

    @Value("${smtp.context}") private String smtpContext;

    @Value("${job.send.email:true}") private Boolean jobSendEmail;

    @Value("${job.upload.dir:/data/geoapi_data/uploads/}") private String jobUploadDir;

    @Value("${job.download.dir:/data/geoapi_data/downloads/}") private String jobDownloadDir;

    @Value("${job.batch.size:40}") private int jobBatchSize;

    @Value("${border.proximity:200}") private int borderProximity;

    @Value("${neighbor.proximity:500}") private int neighborProximity;

    @Value("${district.strategy.single:neighborMatch}") private String districtStrategySingle;

    @Value("${district.strategy.bluebird:streetFallback}") private String districtStrategyBluebird;

    @Value("${district.strategy.batch:streetFallback}") private String districtStrategyBatch;

    @Value("${geocache.enabled:true}") private Boolean geocahceEnabled;

    @Value("${geocache.buffer.size:100}") private int geocahceBufferSize;

    @Value("${nys.geocoder.url:https://gisservices.its.ny.gov/arcgis/rest/services/Locators/Street_and_Address_Composite/GeocodeServer}")
    private String nysGeocoderUrl;

    @Value("${nys.geocode.ext:/findAddressCandidates}") private String nysGeocdeExtension;

    @Value("${nys.revgeocode.ext:/reverseGeocode}") private String nysRevGeocodeExtension;

    @Value("$yahoo.url:http://query.yahooapis.com/v1/yql}") private String yahooUrl;

    @Value("${yahoo.consumer.key:API key obtained from Yahoo}") private String yahooConsumerKey;

    @Value("${yahoo.consumer.secret:API key obtained from Yahoo}") private String yahooConsumerSecret;

    @Value("${yahoo.batch.size:100}") private int yahooBatchSize;

    @Value("${yahoo.boss.url:http://yboss.yahooapis.com/geo/placefinder}") private String yahooBossUrl;

    @Value("${yahoo.boss.consumer_key:API key obtained from Yahoo}") private String yahooBossConsumerKey;

    @Value("${yahoo.boss.consumer_secret:API key obtained from Yahoo}") private String yahooBossConsumerSecret;

    @Value("${osm.url:http://open.mapquestapi.com/nominatim/v1/search}") private String osmUrl;

    @Value("${env.districts.schema:districts}") private String districtsSchema;

    @Value("${env.job.schema:job}") private String jobSchema;

    @Value("${env.log.schema:log}") private String logSchema;

    @Value("${env.public.schema:public}") private String publicSchema;

    @Value("${env.cache.schema:cache}") private String cacheSchema;

    public Environment() {}

    @PostConstruct
    private void init() {

    }

    public String getBaseUrl() {
        return baseUrl.trim();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getSenatorCacheRefreshHours() {
        return senatorCacheRefreshHours;
    }

    public String getUserIpFilter() {
        return userIpFilter.trim();
    }

    public String getUserDefaultKey() {
        return userDefaultKey.trim();
    }

    public String getPublicApiFilter() {
        return publicApiFilter.trim();
    }

    public String getUserPublicKey() {
        return userPublicKey.trim();
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

    public String getUspsAisUrl() {
        return uspsAisUrl.trim();
    }

    public String getUspsAisKey() {
        return uspsAisKey.trim();
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

    public String getGoogleGeocoderUrl() {
        return googleGeocoderUrl.trim();
    }

    public String getGoogleGeocoderKey() {
        return googleGeocoderKey.trim();
    }

    public String getUspsDefault() {
        return uspsDefault.trim();
    }

    public String getGeocoderActive() {
        return geocoderActive.trim();
    }

    public String getGeocoderRank() {
        return geocoderRank.trim();
    }

    public String getGeocoderCacheable() {
        return geocoderCacheable.trim();
    }

    public int getGeocoderRetryInterval() {
        return geocoderRetryInterval;
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

    public Boolean getSmtpActive() {
        return smtpActive;
    }

    public String getSmtpHost() {
        return smtpHost.trim();
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser.trim();
    }

    public String getSmtpPass() {
        return smtpPass.trim();
    }

    public Boolean getSmtpDebug() {
        return smtpDebug;
    }

    public String getSmtpAdmin() {
        return smtpAdmin.trim();
    }

    public Boolean getSmtpTlsEnable() {
        return smtpTlsEnable;
    }

    public Boolean getSmtpSslEnable() {
        return smtpSslEnable;
    }

    public String getSmtpContext() {
        return smtpContext.trim();
    }

    public Boolean getJobSendEmail() {
        return jobSendEmail;
    }

    public String getJobUploadDir() {
        return jobUploadDir.trim();
    }

    public String getJobDownloadDir() {
        return jobDownloadDir.trim();
    }

    public int getJobBatchSize() {
        return jobBatchSize;
    }

    public int getBorderProximity() {
        return borderProximity;
    }

    public int getNeighborProximity() {
        return neighborProximity;
    }

    public String getDistrictStrategySingle() {
        return districtStrategySingle.trim();
    }

    public String getDistrictStrategyBluebird() {
        return districtStrategyBluebird.trim();
    }

    public String getDistrictStrategyBatch() {
        return districtStrategyBatch.trim();
    }

    public Boolean getGeocahceEnabled() {
        return geocahceEnabled;
    }

    public int getGeocahceBufferSize() {
        return geocahceBufferSize;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl.trim();
    }

    public String getGoogleMapsKey() {
        return googleMapsKey.trim();
    }

    public String getNysGeocoderUrl() {
        return nysGeocoderUrl.trim();
    }

    public String getNysGeocdeExtension() {
        return nysGeocdeExtension.trim();
    }

    public String getNysRevGeocodeExtension() {
        return nysRevGeocodeExtension.trim();
    }

    public String getYahooUrl() {
        return yahooUrl.trim();
    }

    public String getYahooConsumerKey() {
        return yahooConsumerKey.trim();
    }

    public String getYahooConsumerSecret() {
        return yahooConsumerSecret.trim();
    }

    public int getYahooBatchSize() {
        return yahooBatchSize;
    }

    public String getYahooBossUrl() {
        return yahooBossUrl.trim();
    }

    public String getYahooBossConsumerKey() {
        return yahooBossConsumerKey.trim();
    }

    public String getYahooBossConsumerSecret() {
        return yahooBossConsumerSecret.trim();
    }

    public String getOsmUrl() {
        return osmUrl.trim();
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

    public String getMassGeocacheErrorLog() {
        return massGeocacheError;
    }
}
