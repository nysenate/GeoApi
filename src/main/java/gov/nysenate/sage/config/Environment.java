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

    @Value("${geocoder.failure.threshold:20}") private int geocoderFailureThreshold;

    @Value("${geocoder.retry.interval:300}") private int geocoderRetryInterval;

    @Value("${tiger.geocoder.timeout:15000}") private int tigerGeocoderTimeout;

    @Value("${validate.threads:3}") private int validateThreads;

    @Value("${distassign.threads:3}") private int distassignThreads;

    @Value("${geocode.threads:3}") private int geocodeThreads;

    @Value("${revgeocode.threads:3}") private int revgeocodeThreads;

    @Value("${geoserver.url:http://geoserver:8080/wfs}") private String geoServerUrl;

    @Value("${geoserver.workspace:nysenate}") private String geoserverWorkspace;

    @Value("${db.driver:org.postgresql.Driver}") private String geoapiDriver;

    @Value("${db.type}") private String geoapiDbType;

    @Value("${db.host}") private String geoapiDbHost;

    @Value("${db.name}") private String geoapiDbName;

    @Value("${db.user}") private String geoapiDbUser;

    @Value("${db.pass}") private String gepapiDbPass;

    @Value("${tiger.db.driver:org.postgresql.Driver}") private String tigerDriver;

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

    @Value("${job.send.email:true}") private Boolean jobSendEmail;

    @Value("${job.upload.dir:/data/geoapi_data/uploads/}") private String jobUploadDir;

    @Value("${job.download.dir:/data/geoapi_data/downloads/}") private String jobDownloadDir;

    @Value("${job.batch.size:40}") private int jobBatchSize;

    @Value("${job.threads.validate:1}") private int jobThreadsValidate;

    @Value("${job.threads.geocode:2}") private int jobThreadsGeocode;

    @Value("${job.threads.distassign:2}") private int jobThreadsDistassign;

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

    @Value("${bing.key:API key obtained from Bing}") private String bingKey;

    @Value("${mapquest.geo.url:http://www.mapquestapi.com/geocoding/v1/batch}") private String mapquestGeoUrl;

    @Value("${mapquest.rev.url:http://www.mapquestapi.com/geocoding/v1/reverse}") private String mapquestRevUrl;

    @Value("${mapquest.key:API key obtained from Mapquest}") private String mapquestKey;

    @Value("${osm.url:http://open.mapquestapi.com/nominatim/v1/search}") private String osmUrl;

    @Value("${ruby.geocoder.url:http://geocoder.nysenate.gov/GeoRubyAdapter/api/geocode}") private String rubyGeocoderUrl;

    @Value("${ruby.geocoder.bulk.urlhttp://geocoder.nysenate.gov/GeoRubyAdapter/api/bulk}") private String rubyGeocoderBulkUrl;

    @Value("${job.process.cron:0 * * * * *}") private String jobProcessCron;

    @Value("${env.districts.schema:districts}") private String districtsSchema;

    @Value("${env.job.schema:job}") private String jobSchema;

    @Value("${env.log.schema:log}") private String logSchema;

    @Value("${env.public.schema:public}") private String publicSchema;

    @Value("${env.cache.schema:cache}") private String cacheSchema;

    @Value("${env.tiger.schema:tiger}") private String tigerSchema;

    @Value("${env.tiger.data.schema:tiger_data}") private String tigerDataSchema;

    @Value("${env.geocoder.public.schema:public}") private String geocoderPublicSchema;

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

    public void setSenatorCacheRefreshHours(int senatorCacheRefreshHours) {
        this.senatorCacheRefreshHours = senatorCacheRefreshHours;
    }

    public String getUserIpFilter() {
        return userIpFilter.trim();
    }

    public void setUserIpFilter(String userIpFilter) {
        this.userIpFilter = userIpFilter;
    }

    public String getUserDefaultKey() {
        return userDefaultKey.trim();
    }

    public void setUserDefaultKey(String userDefaultKey) {
        this.userDefaultKey = userDefaultKey;
    }

    public String getPublicApiFilter() {
        return publicApiFilter.trim();
    }

    public void setPublicApiFilter(String publicApiFilter) {
        this.publicApiFilter = publicApiFilter;
    }

    public String getUserPublicKey() {
        return userPublicKey.trim();
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
        return uspsAisUrl.trim();
    }

    public void setUspsAisUrl(String uspsAisUrl) {
        this.uspsAisUrl = uspsAisUrl;
    }

    public String getUspsAisKey() {
        return uspsAisKey.trim();
    }

    public void setUspsAisKey(String uspsAisKey) {
        this.uspsAisKey = uspsAisKey;
    }

    public String getUspsAmsApiUrl() {
        return uspsAmsApiUrl.trim();
    }

    public void setUspsAmsApiUrl(String uspsAmsApiUrl) {
        this.uspsAmsApiUrl = uspsAmsApiUrl;
    }

    public String getUspsAmsUiUrl() {
        return uspsAmsUiUrl.trim();
    }

    public void setUspsAmsUiUrl(String uspsAmsUiUrl) {
        this.uspsAmsUiUrl = uspsAmsUiUrl;
    }

    public String getNysenateDomain() {
        return nysenateDomain.trim();
    }

    public void setNysenateDomain(String nysenateDomain) {
        this.nysenateDomain = nysenateDomain;
    }

    public String getGoogleGeocoderUrl() {
        return googleGeocoderUrl.trim();
    }

    public void setGoogleGeocoderUrl(String googleGeocoderUrl) {
        this.googleGeocoderUrl = googleGeocoderUrl;
    }

    public String getGoogleGeocoderKey() {
        return googleGeocoderKey.trim();
    }

    public void setGoogleGeocoderKey(String googleGeocoderKey) {
        this.googleGeocoderKey = googleGeocoderKey;
    }

    public String getUspsDefault() {
        return uspsDefault.trim();
    }

    public void setUspsDefault(String uspsDefault) {
        this.uspsDefault = uspsDefault;
    }

    public String getGeocoderActive() {
        return geocoderActive.trim();
    }

    public void setGeocoderActive(String geocoderActive) {
        this.geocoderActive = geocoderActive;
    }

    public String getGeocoderRank() {
        return geocoderRank.trim();
    }

    public void setGeocoderRank(String geocoderRank) {
        this.geocoderRank = geocoderRank;
    }

    public String getGeocoderCacheable() {
        return geocoderCacheable.trim();
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
        return geoServerUrl.trim();
    }

    public void setGeoServerUrl(String geoServerUrl) {
        this.geoServerUrl = geoServerUrl;
    }

    public String getGeoserverWorkspace() {
        return geoserverWorkspace.trim();
    }

    public void setGeoserverWorkspace(String geoserverWorkspace) {
        this.geoserverWorkspace = geoserverWorkspace;
    }

    public String getGeoapiDriver() {
        return geoapiDriver.trim();
    }

    public void setGeoapiDriver(String geoapiDriver) {
        this.geoapiDriver = geoapiDriver;
    }

    public String getGeoapiDbType() {
        return geoapiDbType.trim();
    }

    public void setGeoapiDbType(String geoapiDbType) {
        this.geoapiDbType = geoapiDbType;
    }

    public String getGeoapiDbHost() {
        return geoapiDbHost.trim();
    }

    public void setGeoapiDbHost(String geoapiDbHost) {
        this.geoapiDbHost = geoapiDbHost;
    }

    public String getGeoapiDbName() {
        return geoapiDbName.trim();
    }

    public void setGeoapiDbName(String geoapiDbName) {
        this.geoapiDbName = geoapiDbName;
    }

    public String getGeoapiDbUser() {
        return geoapiDbUser.trim();
    }

    public void setGeoapiDbUser(String geoapiDbUser) {
        this.geoapiDbUser = geoapiDbUser;
    }

    public String getGepapiDbPass() {
        return gepapiDbPass.trim();
    }

    public void setGepapiDbPass(String gepapiDbPass) {
        this.gepapiDbPass = gepapiDbPass;
    }

    public String getTigerDriver() {
        return tigerDriver.trim();
    }

    public void setTigerDriver(String tigerDriver) {
        this.tigerDriver = tigerDriver;
    }

    public String getTigerDbType() {
        return tigerDbType.trim();
    }

    public void setTigerDbType(String tigerDbType) {
        this.tigerDbType = tigerDbType;
    }

    public String getTigerDbHost() {
        return tigerDbHost.trim();
    }

    public void setTigerDbHost(String tigerDbHost) {
        this.tigerDbHost = tigerDbHost;
    }

    public String getTigerDbName() {
        return tigerDbName.trim();
    }

    public void setTigerDbName(String tigerDbName) {
        this.tigerDbName = tigerDbName;
    }

    public String getTigerDbUser() {
        return tigerDbUser.trim();
    }

    public void setTigerDbUser(String tigerDbUser) {
        this.tigerDbUser = tigerDbUser;
    }

    public String getTigerDbPass() {
        return tigerDbPass.trim();
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
        return smtpHost.trim();
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
        return smtpUser.trim();
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public String getSmtpPass() {
        return smtpPass.trim();
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
        return smtpAdmin.trim();
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
        return smtpContext.trim();
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
        return jobUploadDir.trim();
    }

    public void setJobUploadDir(String jobUploadDir) {
        this.jobUploadDir = jobUploadDir;
    }

    public String getJobDownloadDir() {
        return jobDownloadDir.trim();
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
        return districtStrategySingle.trim();
    }

    public void setDistrictStrategySingle(String districtStrategySingle) {
        this.districtStrategySingle = districtStrategySingle;
    }

    public String getDistrictStrategyBluebird() {
        return districtStrategyBluebird.trim();
    }

    public void setDistrictStrategyBluebird(String districtStrategyBluebird) {
        this.districtStrategyBluebird = districtStrategyBluebird;
    }

    public String getDistrictStrategyBatch() {
        return districtStrategyBatch.trim();
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
        return googleMapsUrl.trim();
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }

    public String getGoogleMapsKey() {
        return googleMapsKey.trim();
    }

    public void setGoogleMapsKey(String googleMapsKey) {
        this.googleMapsKey = googleMapsKey;
    }

    public String getNysGeocoderUrl() {
        return nysGeocoderUrl.trim();
    }

    public void setNysGeocoderUrl(String nysGeocoderUrl) {
        this.nysGeocoderUrl = nysGeocoderUrl;
    }

    public String getNysGeocdeExtension() {
        return nysGeocdeExtension.trim();
    }

    public void setNysGeocdeExtension(String nysGeocdeExtension) {
        this.nysGeocdeExtension = nysGeocdeExtension;
    }

    public String getNysRevGeocodeExtension() {
        return nysRevGeocodeExtension.trim();
    }

    public void setNysRevGeocodeExtension(String nysRevGeocodeExtension) {
        this.nysRevGeocodeExtension = nysRevGeocodeExtension;
    }

    public String getYahooUrl() {
        return yahooUrl.trim();
    }

    public void setYahooUrl(String yahooUrl) {
        this.yahooUrl = yahooUrl;
    }

    public String getYahooConsumerKey() {
        return yahooConsumerKey.trim();
    }

    public void setYahooConsumerKey(String yahooConsumerKey) {
        this.yahooConsumerKey = yahooConsumerKey;
    }

    public String getYahooConsumerSecret() {
        return yahooConsumerSecret.trim();
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
        return yahooBossUrl.trim();
    }

    public void setYahooBossUrl(String yahooBossUrl) {
        this.yahooBossUrl = yahooBossUrl;
    }

    public String getYahooBossConsumerKey() {
        return yahooBossConsumerKey.trim();
    }

    public void setYahooBossConsumerKey(String yahooBossConsumerKey) {
        this.yahooBossConsumerKey = yahooBossConsumerKey;
    }

    public String getYahooBossConsumerSecret() {
        return yahooBossConsumerSecret.trim();
    }

    public void setYahooBossConsumerSecret(String yahooBossConsumerSecret) {
        this.yahooBossConsumerSecret = yahooBossConsumerSecret;
    }

    public String getBingKey() {
        return bingKey.trim();
    }

    public void setBingKey(String bingKey) {
        this.bingKey = bingKey;
    }

    public String getMapquestGeoUrl() {
        return mapquestGeoUrl.trim();
    }

    public void setMapquestGeoUrl(String mapquestGeoUrl) {
        this.mapquestGeoUrl = mapquestGeoUrl;
    }

    public String getMapquestRevUrl() {
        return mapquestRevUrl.trim();
    }

    public void setMapquestRevUrl(String mapquestRevUrl) {
        this.mapquestRevUrl = mapquestRevUrl;
    }

    public String getMapquestKey() {
        return mapquestKey.trim();
    }

    public void setMapquestKey(String mapquestKey) {
        this.mapquestKey = mapquestKey;
    }

    public String getOsmUrl() {
        return osmUrl.trim();
    }

    public void setOsmUrl(String osmUrl) {
        this.osmUrl = osmUrl;
    }

    public String getRubyGeocoderUrl() {
        return rubyGeocoderUrl.trim();
    }

    public void setRubyGeocoderUrl(String rubyGeocoderUrl) {
        this.rubyGeocoderUrl = rubyGeocoderUrl;
    }

    public String getRubyGeocoderBulkUrl() {
        return rubyGeocoderBulkUrl.trim();
    }

    public void setRubyGeocoderBulkUrl(String rubyGeocoderBulkUrl) {
        this.rubyGeocoderBulkUrl = rubyGeocoderBulkUrl;
    }

    public String getJobProcessCron() {
        return jobProcessCron.trim();
    }

    public void setJobProcessCron(String jobProcessCron) {
        this.jobProcessCron = jobProcessCron;
    }

    public String getDistrictsSchema() {
        return districtsSchema.trim();
    }

    public void setDistrictsSchema(String districtsSchema) {
        this.districtsSchema = districtsSchema;
    }

    public String getJobSchema() {
        return jobSchema.trim();
    }

    public void setJobSchema(String jobSchema) {
        this.jobSchema = jobSchema;
    }

    public String getLogSchema() {
        return logSchema.trim();
    }

    public void setLogSchema(String logSchema) {
        this.logSchema = logSchema;
    }

    public String getPublicSchema() {
        return publicSchema.trim();
    }

    public void setPublicSchema(String publicSchema) {
        this.publicSchema = publicSchema;
    }

    public String getCacheSchema() {
        return cacheSchema.trim();
    }

    public void setCacheSchema(String cacheSchema) {
        this.cacheSchema = cacheSchema;
    }

    public String getTigerSchema() {
        return tigerSchema.trim();
    }

    public void setTigerSchema(String tigerSchema) {
        this.tigerSchema = tigerSchema;
    }

    public String getTigerDataSchema() {
        return tigerDataSchema.trim();
    }

    public void setTigerDataSchema(String tigerDataSchema) {
        this.tigerDataSchema = tigerDataSchema;
    }

    public String getGeocoderPublicSchema() {
        return geocoderPublicSchema.trim();
    }

    public void setGeocoderPublicSchema(String geocoderPublicSchema) {
        this.geocoderPublicSchema = geocoderPublicSchema;
    }

    public String getMassGeocacheErrorLog() {
        return massGeocacheError;
    }

    public void setMassGeocacheErrorLog(String massGeocacheError) {
        this.massGeocacheError = massGeocacheError;
    }
}
