package gov.nysenate.sage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Property config that will execute only when the spring profile is in 'test' mode.
 * This allows for loading test.app.properties for unit tests.
 */
@Configuration
@PropertySource("classpath:/app.properties")
@PropertySource("classpath:/test.app.properties")
@Profile({"test"})
public class TestConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        var pspc = new PropertySourcesPlaceholderConfigurer();
        pspc.setIgnoreUnresolvablePlaceholders(true);
        return pspc;
    }
}
