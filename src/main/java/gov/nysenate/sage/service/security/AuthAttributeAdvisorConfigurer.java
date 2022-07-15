
package gov.nysenate.sage.service.security;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
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
public class AuthAttributeAdvisorConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(AuthRealmConfigurer.class);

    @Autowired
    @Qualifier("AuthAttributeAdvisor")
    protected AuthorizationAttributeSourceAdvisor advisor;

    @Autowired
    @Qualifier("securityManager")
    protected DefaultWebSecurityManager securityManager;

//    @Autowired
//    public AuthAttributeAdvisorConfigurer(AuthorizationAttributeSourceAdvisor advisor, DefaultWebSecurityManager securityManager) {
//        this.advisor = advisor;
//        this.securityManager = securityManager;
//    }

    public AuthAttributeAdvisorConfigurer(){}

    @PostConstruct
    public void setUp() {
        advisor.setSecurityManager(this.securityManager);
    }
}