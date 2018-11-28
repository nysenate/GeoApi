package gov.nysenate.sage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("environment")
public class Environment {

    @Value("${base.url}") private String baseUrl;

    @Value("${init.caches}") private boolean initCaches;

    @Value("${senator.cache.refresh.hours}") private int senatorCacheRefreshHours;

    @Value("${user.ip.filter}") private String userIpFilter;

    @Value("${user.default.key}") private String userDefaultKey;

    @Value("${public.api.filter}") private String publicApiFilter;

    @Value("${user.public.key}") private String userPublicKey;

    @Value("${api.logging.enabled}") private boolean apiLoggingEnabled;

    @Value("${detailed.logging.enabled}") private boolean detailedLoggingEnabled;

    @Value("${batch.detailed.logging.enabled}") private boolean batchDetailedLoggingEnabled;

    @Value("${usps.ais.url}") private String uspsAisUrl;

    @Value("${usps.ais.key}") private String uspsAisKey;

    @Value("${usps.ams.api.url}") private String uspsAmsApiUrl;

    @Value("${usps.ams.ui.url}") private String uspsAmsUiUrl;

    @Value("${nysenate.domain}") private String nysenateDomain;

    @Value("${google.geocoder.url}") private String googleGeocoderUrl;

    @Value("${google.geocoder.key}") private String googleGeocoderKey;

    @Value("${google.maps.url}") private String googleMapsUrl;

    @Value("${google.maps.key}") private String googleMapsKey;

    @Value("${usps.default}") private String uspsDefault;

    @Value("${geocoder.active}") private String geocoderActive;

    @Value("${geocoder.rank}") private String geocoderRank;

    @Value("${geocoder.cacheable}") private String geocoderCacheable;

    @Value("${geocoder.failure.threshold}") private int geocoderFailureThreshold;

    @Value("${geocoder.retry.interval}") private int geocoderRetryInterval;

    @Value("${tiger.geocoder.timeout}") private int tigerGeocoderTimeout;

    @Value("${validate.threads}") private int validateThreads;

    @Value("${distassign.threads}") private int distassignThreads;

    @Value("${geocode.threads}") private int geocodeThreads;

    @Value("${revgeocode.threads}") private int revgeocodeThreads;

    @Value("${geoserver.url}") private String geoServerUrl;

    @Value("${geoserver.workspace}") private String geoserverWorkspace;

    @Value("${db.driver}") private String geoapiDriver;

    @Value("${db.type}") private String geoapiDbType;

    @Value("${db.host}") private String geoapiDbHost;

    @Value("${db.name}") private String geoapiDbName;

    @Value("${db.user}") private String geoapiDbUser;

    @Value("${db.pass}") private String gepapiDbPass;

    @Value("${tiger.db.driver}") private String tigerDriver;

    @Value("${tiger.db.type}") private String tigerDbType;

    @Value("${tiger.db.host}") private String tigerDbHost;

    @Value("${tiger.db.name}") private String tigerDbName;

    @Value("${tiger.db.user}") private String tigerDbUser;

    @Value("${tiger.db.pass}") private String tigerDbPass;

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

    @Value("${job.send.email}") private Boolean jobSendEmail;

    @Value("${job.upload.dir}") private String jobUploadDir;

    @Value("${job.download.dir}") private String jobDownloadDir;

    @Value("${job.batch.size}") private int jobBatchSize;

    @Value("${job.threads.validate}") private int jobThreadsValidate;

    @Value("${job.threads.geocode}") private int jobThreadsGeocode;

    @Value("${job.threads.distassign}") private int jobThreadsDistassign;

    @Value("${border.proximity}") private int borderProximity;

    @Value("${neighbor.proximity}") private int neighborProximity;

    @Value("${district.strategy.single}") private String districtStrategySingle;

    @Value("${district.strategy.bluebird}") private String districtStrategyBluebird;

    @Value("${district.strategy.batch}") private String districtStrategyBatch;

    @Value("${geocache.enabled}") private Boolean geocahceEnabled;

    @Value("${geocache.buffer.size}") private int geocahceBufferSize;

    @Value("${nys.geocoder.url}") private String nysGeocoderUrl;

    @Value("${nys.geocode.ext}") private String nysGeocdeExtension;

    @Value("${nys.revgeocode.ext}") private String nysRevGeocodeExtension;

    @Value("$yahoo.url}") private String yahooUrl;

    @Value("${yahoo.consumer.key}") private String yahooConsumerKey;

    @Value("${yahoo.consumer.secret}") private String yahooConsumerSecret;

    @Value("${yahoo.batch.size}") private int yahooBatchSize;

    @Value("${yahoo.boss.url}") private String yahooBossUrl;

    @Value("${yahoo.boss.consumer_key}") private String yahooBossConsumerKey;

    @Value("${yahoo.boss.consumer_secret}") private String yahooBossConsumerSecret;

    @Value("${bing.key}") private String bingKey;

    @Value("${mapquest.geo.url}") private String mapquestGeoUrl;

    @Value("${mapquest.rev.url}") private String mapquestRevUrl;

    @Value("${mapquest.key}") private String mapquestKey;

    @Value("${osm.url}") private String osmUrl;

    @Value("${ruby.geocoder.url}") private String rubyGeocoderUrl;

    @Value("${ruby.geocoder.bulk.url}") private String rubyGeocoderBulkUrl;

    public Environment() {}

    @PostConstruct
    private void init() {

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isInitCaches() {
        return initCaches;
    }

    public void setInitCaches(boolean initCaches) {
        this.initCaches = initCaches;
    }

    public int getSenatorCacheRefreshHours() {
        return senatorCacheRefreshHours;
    }

    public void setSenatorCacheRefreshHours(int senatorCacheRefreshHours) {
        this.senatorCacheRefreshHours = senatorCacheRefreshHours;
    }

    public String getUserIpFilter() {
        return userIpFilter;
    }

    public void setUserIpFilter(String userIpFilter) {
        this.userIpFilter = userIpFilter;
    }

    public String getUserDefaultKey() {
        return userDefaultKey;
    }

    public void setUserDefaultKey(String userDefaultKey) {
        this.userDefaultKey = userDefaultKey;
    }

    public String getPublicApiFilter() {
        return publicApiFilter;
    }

    public void setPublicApiFilter(String publicApiFilter) {
        this.publicApiFilter = publicApiFilter;
    }

    public String getUserPublicKey() {
        return userPublicKey;
    }

    public void setUserPublicKey(String userPublicKey) {
        this.userPublicKey = userPublicKey;
    }

    public boolean isApiLoggingEnabled() {
        return apiLoggingEnabled;
    }

    public void setApiLoggingEnabled(boolean apiLoggingEnabled) {
        this.apiLoggingEnabled = apiLoggingEnabled;
    }

    public boolean isDetailedLoggingEnabled() {
        return detailedLoggingEnabled;
    }

    public void setDetailedLoggingEnabled(boolean detailedLoggingEnabled) {
        this.detailedLoggingEnabled = detailedLoggingEnabled;
    }

    public boolean isBatchDetailedLoggingEnabled() {
        return batchDetailedLoggingEnabled;
    }

    public void setBatchDetailedLoggingEnabled(boolean batchDetailedLoggingEnabled) {
        this.batchDetailedLoggingEnabled = batchDetailedLoggingEnabled;
    }

    public String getUspsAisUrl() {
        return uspsAisUrl;
    }

    public void setUspsAisUrl(String uspsAisUrl) {
        this.uspsAisUrl = uspsAisUrl;
    }

    public String getUspsAisKey() {
        return uspsAisKey;
    }

    public void setUspsAisKey(String uspsAisKey) {
        this.uspsAisKey = uspsAisKey;
    }

    public String getUspsAmsApiUrl() {
        return uspsAmsApiUrl;
    }

    public void setUspsAmsApiUrl(String uspsAmsApiUrl) {
        this.uspsAmsApiUrl = uspsAmsApiUrl;
    }

    public String getUspsAmsUiUrl() {
        return uspsAmsUiUrl;
    }

    public void setUspsAmsUiUrl(String uspsAmsUiUrl) {
        this.uspsAmsUiUrl = uspsAmsUiUrl;
    }

    public String getNysenateDomain() {
        return nysenateDomain;
    }

    public void setNysenateDomain(String nysenateDomain) {
        this.nysenateDomain = nysenateDomain;
    }

    public String getGoogleGeocoderUrl() {
        return googleGeocoderUrl;
    }

    public void setGoogleGeocoderUrl(String googleGeocoderUrl) {
        this.googleGeocoderUrl = googleGeocoderUrl;
    }

    public String getGoogleGeocoderKey() {
        return googleGeocoderKey;
    }

    public void setGoogleGeocoderKey(String googleGeocoderKey) {
        this.googleGeocoderKey = googleGeocoderKey;
    }

    public String getUspsDefault() {
        return uspsDefault;
    }

    public void setUspsDefault(String uspsDefault) {
        this.uspsDefault = uspsDefault;
    }

    public String getGeocoderActive() {
        return geocoderActive;
    }

    public void setGeocoderActive(String geocoderActive) {
        this.geocoderActive = geocoderActive;
    }

    public String getGeocoderRank() {
        return geocoderRank;
    }

    public void setGeocoderRank(String geocoderRank) {
        this.geocoderRank = geocoderRank;
    }

    public String getGeocoderCacheable() {
        return geocoderCacheable;
    }

    public void setGeocoderCacheable(String geocoderCacheable) {
        this.geocoderCacheable = geocoderCacheable;
    }

    public int getGeocoderFailureThreshold() {
        return geocoderFailureThreshold;
    }

    public void setGeocoderFailureThreshold(int geocoderFailureThreshold) {
        this.geocoderFailureThreshold = geocoderFailureThreshold;
    }

    public int getGeocoderRetryInterval() {
        return geocoderRetryInterval;
    }

    public void setGeocoderRetryInterval(int geocoderRetryInterval) {
        this.geocoderRetryInterval = geocoderRetryInterval;
    }

    public int getTigerGeocoderTimeout() {
        return tigerGeocoderTimeout;
    }

    public void setTigerGeocoderTimeout(int tigerGeocoderTimeout) {
        this.tigerGeocoderTimeout = tigerGeocoderTimeout;
    }

    public int getValidateThreads() {
        return validateThreads;
    }

    public void setValidateThreads(int validateThreads) {
        this.validateThreads = validateThreads;
    }

    public int getDistassignThreads() {
        return distassignThreads;
    }

    public void setDistassignThreads(int distassignThreads) {
        this.distassignThreads = distassignThreads;
    }

    public int getGeocodeThreads() {
        return geocodeThreads;
    }

    public void setGeocodeThreads(int geocodeThreads) {
        this.geocodeThreads = geocodeThreads;
    }

    public int getRevgeocodeThreads() {
        return revgeocodeThreads;
    }

    public void setRevgeocodeThreads(int revgeocodeThreads) {
        this.revgeocodeThreads = revgeocodeThreads;
    }

    public String getGeoServerUrl() {
        return geoServerUrl;
    }

    public void setGeoServerUrl(String geoServerUrl) {
        this.geoServerUrl = geoServerUrl;
    }

    public String getGeoserverWorkspace() {
        return geoserverWorkspace;
    }

    public void setGeoserverWorkspace(String geoserverWorkspace) {
        this.geoserverWorkspace = geoserverWorkspace;
    }

    public String getGeoapiDriver() {
        return geoapiDriver;
    }

    public void setGeoapiDriver(String geoapiDriver) {
        this.geoapiDriver = geoapiDriver;
    }

    public String getGeoapiDbType() {
        return geoapiDbType;
    }

    public void setGeoapiDbType(String geoapiDbType) {
        this.geoapiDbType = geoapiDbType;
    }

    public String getGeoapiDbHost() {
        return geoapiDbHost;
    }

    public void setGeoapiDbHost(String geoapiDbHost) {
        this.geoapiDbHost = geoapiDbHost;
    }

    public String getGeoapiDbName() {
        return geoapiDbName;
    }

    public void setGeoapiDbName(String geoapiDbName) {
        this.geoapiDbName = geoapiDbName;
    }

    public String getGeoapiDbUser() {
        return geoapiDbUser;
    }

    public void setGeoapiDbUser(String geoapiDbUser) {
        this.geoapiDbUser = geoapiDbUser;
    }

    public String getGepapiDbPass() {
        return gepapiDbPass;
    }

    public void setGepapiDbPass(String gepapiDbPass) {
        this.gepapiDbPass = gepapiDbPass;
    }

    public String getTigerDriver() {
        return tigerDriver;
    }

    public void setTigerDriver(String tigerDriver) {
        this.tigerDriver = tigerDriver;
    }

    public String getTigerDbType() {
        return tigerDbType;
    }

    public void setTigerDbType(String tigerDbType) {
        this.tigerDbType = tigerDbType;
    }

    public String getTigerDbHost() {
        return tigerDbHost;
    }

    public void setTigerDbHost(String tigerDbHost) {
        this.tigerDbHost = tigerDbHost;
    }

    public String getTigerDbName() {
        return tigerDbName;
    }

    public void setTigerDbName(String tigerDbName) {
        this.tigerDbName = tigerDbName;
    }

    public String getTigerDbUser() {
        return tigerDbUser;
    }

    public void setTigerDbUser(String tigerDbUser) {
        this.tigerDbUser = tigerDbUser;
    }

    public String getTigerDbPass() {
        return tigerDbPass;
    }

    public void setTigerDbPass(String tigerDbPass) {
        this.tigerDbPass = tigerDbPass;
    }

    public Boolean getSmtpActive() {
        return smtpActive;
    }

    public void setSmtpActive(Boolean smtpActive) {
        this.smtpActive = smtpActive;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public String getSmtpPass() {
        return smtpPass;
    }

    public void setSmtpPass(String smtpPass) {
        this.smtpPass = smtpPass;
    }

    public Boolean getSmtpDebug() {
        return smtpDebug;
    }

    public void setSmtpDebug(Boolean smtpDebug) {
        this.smtpDebug = smtpDebug;
    }

    public String getSmtpAdmin() {
        return smtpAdmin;
    }

    public void setSmtpAdmin(String smtpAdmin) {
        this.smtpAdmin = smtpAdmin;
    }

    public Boolean getSmtpTlsEnable() {
        return smtpTlsEnable;
    }

    public void setSmtpTlsEnable(Boolean smtpTlsEnable) {
        this.smtpTlsEnable = smtpTlsEnable;
    }

    public Boolean getSmtpSslEnable() {
        return smtpSslEnable;
    }

    public void setSmtpSslEnable(Boolean smtpSslEnable) {
        this.smtpSslEnable = smtpSslEnable;
    }

    public String getSmtpContext() {
        return smtpContext;
    }

    public void setSmtpContext(String smtpContext) {
        this.smtpContext = smtpContext;
    }

    public Boolean getJobSendEmail() {
        return jobSendEmail;
    }

    public void setJobSendEmail(Boolean jobSendEmail) {
        this.jobSendEmail = jobSendEmail;
    }

    public String getJobUploadDir() {
        return jobUploadDir;
    }

    public void setJobUploadDir(String jobUploadDir) {
        this.jobUploadDir = jobUploadDir;
    }

    public String getJobDownloadDir() {
        return jobDownloadDir;
    }

    public void setJobDownloadDir(String jobDownloadDir) {
        this.jobDownloadDir = jobDownloadDir;
    }

    public int getJobBatchSize() {
        return jobBatchSize;
    }

    public void setJobBatchSize(int jobBatchSize) {
        this.jobBatchSize = jobBatchSize;
    }

    public int getJobThreadsValidate() {
        return jobThreadsValidate;
    }

    public void setJobThreadsValidate(int jobThreadsValidate) {
        this.jobThreadsValidate = jobThreadsValidate;
    }

    public int getJobThreadsGeocode() {
        return jobThreadsGeocode;
    }

    public void setJobThreadsGeocode(int jobThreadsGeocode) {
        this.jobThreadsGeocode = jobThreadsGeocode;
    }

    public int getJobThreadsDistassign() {
        return jobThreadsDistassign;
    }

    public void setJobThreadsDistassign(int jobThreadsDistassign) {
        this.jobThreadsDistassign = jobThreadsDistassign;
    }

    public int getBorderProximity() {
        return borderProximity;
    }

    public void setBorderProximity(int borderProximity) {
        this.borderProximity = borderProximity;
    }

    public int getNeighborProximity() {
        return neighborProximity;
    }

    public void setNeighborProximity(int neighborProximity) {
        this.neighborProximity = neighborProximity;
    }

    public String getDistrictStrategySingle() {
        return districtStrategySingle;
    }

    public void setDistrictStrategySingle(String districtStrategySingle) {
        this.districtStrategySingle = districtStrategySingle;
    }

    public String getDistrictStrategyBluebird() {
        return districtStrategyBluebird;
    }

    public void setDistrictStrategyBluebird(String districtStrategyBluebird) {
        this.districtStrategyBluebird = districtStrategyBluebird;
    }

    public String getDistrictStrategyBatch() {
        return districtStrategyBatch;
    }

    public void setDistrictStrategyBatch(String districtStrategyBatch) {
        this.districtStrategyBatch = districtStrategyBatch;
    }

    public Boolean getGeocahceEnabled() {
        return geocahceEnabled;
    }

    public void setGeocahceEnabled(Boolean geocahceEnabled) {
        this.geocahceEnabled = geocahceEnabled;
    }

    public int getGeocahceBufferSize() {
        return geocahceBufferSize;
    }

    public void setGeocahceBufferSize(int geocahceBufferSize) {
        this.geocahceBufferSize = geocahceBufferSize;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }

    public String getGoogleMapsKey() {
        return googleMapsKey;
    }

    public void setGoogleMapsKey(String googleMapsKey) {
        this.googleMapsKey = googleMapsKey;
    }

    public String getNysGeocoderUrl() {
        return nysGeocoderUrl;
    }

    public void setNysGeocoderUrl(String nysGeocoderUrl) {
        this.nysGeocoderUrl = nysGeocoderUrl;
    }

    public String getNysGeocdeExtension() {
        return nysGeocdeExtension;
    }

    public void setNysGeocdeExtension(String nysGeocdeExtension) {
        this.nysGeocdeExtension = nysGeocdeExtension;
    }

    public String getNysRevGeocodeExtension() {
        return nysRevGeocodeExtension;
    }

    public void setNysRevGeocodeExtension(String nysRevGeocodeExtension) {
        this.nysRevGeocodeExtension = nysRevGeocodeExtension;
    }

    public String getYahooUrl() {
        return yahooUrl;
    }

    public void setYahooUrl(String yahooUrl) {
        this.yahooUrl = yahooUrl;
    }

    public String getYahooConsumerKey() {
        return yahooConsumerKey;
    }

    public void setYahooConsumerKey(String yahooConsumerKey) {
        this.yahooConsumerKey = yahooConsumerKey;
    }

    public String getYahooConsumerSecret() {
        return yahooConsumerSecret;
    }

    public void setYahooConsumerSecret(String yahooConsumerSecret) {
        this.yahooConsumerSecret = yahooConsumerSecret;
    }

    public int getYahooBatchSize() {
        return yahooBatchSize;
    }

    public void setYahooBatchSize(int yahooBatchSize) {
        this.yahooBatchSize = yahooBatchSize;
    }

    public String getYahooBossUrl() {
        return yahooBossUrl;
    }

    public void setYahooBossUrl(String yahooBossUrl) {
        this.yahooBossUrl = yahooBossUrl;
    }

    public String getYahooBossConsumerKey() {
        return yahooBossConsumerKey;
    }

    public void setYahooBossConsumerKey(String yahooBossConsumerKey) {
        this.yahooBossConsumerKey = yahooBossConsumerKey;
    }

    public String getYahooBossConsumerSecret() {
        return yahooBossConsumerSecret;
    }

    public void setYahooBossConsumerSecret(String yahooBossConsumerSecret) {
        this.yahooBossConsumerSecret = yahooBossConsumerSecret;
    }

    public String getBingKey() {
        return bingKey;
    }

    public void setBingKey(String bingKey) {
        this.bingKey = bingKey;
    }

    public String getMapquestGeoUrl() {
        return mapquestGeoUrl;
    }

    public void setMapquestGeoUrl(String mapquestGeoUrl) {
        this.mapquestGeoUrl = mapquestGeoUrl;
    }

    public String getMapquestRevUrl() {
        return mapquestRevUrl;
    }

    public void setMapquestRevUrl(String mapquestRevUrl) {
        this.mapquestRevUrl = mapquestRevUrl;
    }

    public String getMapquestKey() {
        return mapquestKey;
    }

    public void setMapquestKey(String mapquestKey) {
        this.mapquestKey = mapquestKey;
    }

    public String getOsmUrl() {
        return osmUrl;
    }

    public void setOsmUrl(String osmUrl) {
        this.osmUrl = osmUrl;
    }

    public String getRubyGeocoderUrl() {
        return rubyGeocoderUrl;
    }

    public void setRubyGeocoderUrl(String rubyGeocoderUrl) {
        this.rubyGeocoderUrl = rubyGeocoderUrl;
    }

    public String getRubyGeocoderBulkUrl() {
        return rubyGeocoderBulkUrl;
    }

    public void setRubyGeocoderBulkUrl(String rubyGeocoderBulkUrl) {
        this.rubyGeocoderBulkUrl = rubyGeocoderBulkUrl;
    }
}
