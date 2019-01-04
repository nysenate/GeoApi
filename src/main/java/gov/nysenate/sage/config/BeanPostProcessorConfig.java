package gov.nysenate.sage.config;

import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans that implement {@link BeanPostProcessor} are configured here.
 * These beans must be configured separately because they are configured earlier in the process than most other beans.
 * This causes any other beans in the shared config class to be configured at the same time,
 *  which may occur before <code>@Autowired</code> or <code>@Value</code> aspects are evaluated
 */
@Configuration
public class BeanPostProcessorConfig {

    /**
     * Enables proxy creation for use in annotation based AOP
     * This is needed for Shiro annotations to work.
     * @return DefaultAdvisorAutoProxyCreator
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    /**
     * Integrates Apache Shiro with Spring
     * @return LifecycleBeanPostProcessor
     */
    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }
}
