package gov.nysenate.sage.service.security;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class AuthRealmConfigurer
{
    private static final Logger logger = LoggerFactory.getLogger(AuthRealmConfigurer.class);

    protected List<Realm> realmList;
    protected DefaultWebSecurityManager securityManager;

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