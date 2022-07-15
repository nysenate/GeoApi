package gov.nysenate.sage.service.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ShiroFilterConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AuthRealmConfigurer.class);


    @Autowired
    @Qualifier("shiroFilter")
    protected ShiroFilterFactoryBean shiroFilter;

    @Autowired
    @Qualifier("securityManager")
    protected DefaultWebSecurityManager securityManager;

    public ShiroFilterConfigurer() {}

    @PostConstruct
    public void setUp() {
        shiroFilter.setSecurityManager(this.securityManager);
        SecurityUtils.setSecurityManager(this.securityManager);
    }
}