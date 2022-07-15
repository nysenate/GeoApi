package gov.nysenate.sage.service.security;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class AuthRealmConfigurer
{
    private static final Logger logger = LoggerFactory.getLogger(AuthRealmConfigurer.class);

    @Autowired
    protected List<Realm> realmList;
    @Autowired
    @Qualifier("securityManager")
    protected DefaultWebSecurityManager securityManager;

    public AuthRealmConfigurer(){}

    @PostConstruct
    public void setUp() {
        securityManager.setRealms(realmList);
    }
}