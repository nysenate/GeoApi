package gov.nysenate.sage.model.api;

import gov.nysenate.sage.util.FormatUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the uri information that is provided to request a service from the API. This object is
 * typically created by the API filter and consumed by the controller classes.
 */
@Getter
public class ApiRequest {
    private static final Logger logger = LoggerFactory.getLogger(ApiRequest.class);
    private static final Pattern validFormat = Pattern.compile("/api/v\\d+/(?<service>\\w+)/(?<request>\\w+(/\\w+)?)");

    private final boolean isValid;
    @Setter
    private ApiUser apiUser;
    private String hostAddress;

    /** Uri attributes */
    private String service;
    private String request;
    private final String params;

    public ApiRequest(HttpServletRequest request) {
        // getRemoteAddr() is the authoritative client IP: RemoteIpFilter (see WebInitializer)
        // resolves X-Forwarded-For from trusted proxies only, so the header cannot be spoofed
        // by an untrusted client to impersonate an internal address.
        String remoteIp = request.getRemoteAddr();
        // Resolve IP address into InetAddress
        try {
            this.hostAddress = InetAddress.getByName(remoteIp).getHostAddress();
        }
        catch (UnknownHostException ex) {
            logger.warn("Unknown remote ip host!", ex);
        }
        Matcher matcher = validFormat.matcher(request.getRequestURI());
        this.isValid = matcher.matches();
        if (isValid) {
            this.service = FormatUtil.cleanString(matcher.group("service"));
            this.request = FormatUtil.cleanString(matcher.group("request"));
        }
        var paramList = new ArrayList<String>();
        for (var entry : request.getParameterMap().entrySet()) {
            paramList.add(entry.getKey() + "=" + Arrays.toString(entry.getValue()));
        }
        this.params = String.join("&", paramList);
    }
}
