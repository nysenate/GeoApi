package gov.nysenate.sage.service.security;

import jakarta.annotation.PostConstruct;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthRealmConfigurer {
    private final List<Realm> realmList;
    private final DefaultWebSecurityManager securityManager;

    @Autowired
    public AuthRealmConfigurer(List<Realm> realmList, DefaultWebSecurityManager securityManager) {
        this.realmList = realmList;
        this.securityManager = securityManager;
    }

    @PostConstruct
    public void setUp() {
        securityManager.setRealms(realmList);
    }
}