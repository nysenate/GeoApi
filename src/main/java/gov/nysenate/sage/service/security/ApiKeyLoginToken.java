package gov.nysenate.sage.service.security;


import org.apache.shiro.authc.HostAuthenticationToken;

public record ApiKeyLoginToken(String apiKey, String host) implements HostAuthenticationToken {
    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
