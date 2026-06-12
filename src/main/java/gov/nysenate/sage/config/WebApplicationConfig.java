package gov.nysenate.sage.config;

import gov.nysenate.sage.dao.logger.deployment.SqlDeploymentLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Locale;

@Configuration
@EnableWebMvc
@EnableAsync
@EnableScheduling
@ComponentScan("gov.nysenate.sage")
@Import({DatabaseConfig.class, SecurityConfig.class, ApplicationConfig.class })
public class WebApplicationConfig implements WebMvcConfigurer {
    private static final Logger logger = LogManager.getLogger(WebApplicationConfig.class);
    // ASCII Art generated at http://patorjk.com/software/taag/
    // SAGE FONT = USA FLAG
    private static final String ASCII_ART = """
                      :::===  :::====  :::=====  :::=====   \s
                      :::     :::  === :::       :::        \s
                       =====  ======== === ===== ======     \s
                          === ===  === ===   === ===        \s
                      ======  ===  ===  =======  ========   \s
                    ======================================= \s
                      DEPLOYED ON %s                    \s
                    =======================================\
            """;

    private final SqlDeploymentLogger sqlDeploymentLogger;

    @Autowired
    public WebApplicationConfig(SqlDeploymentLogger sqlDeploymentLogger) {
        this.sqlDeploymentLogger = sqlDeploymentLogger;
    }

    @PostConstruct
    public void init() {
        logger.info("\n{}", ASCII_ART.formatted(LocalDateTime.now().toString()));
        sqlDeploymentLogger.logDeploymentStatus();
    }

    /** Sets paths that should not be intercepted by a controller (e.g css/ js/). */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/js/**").addResourceLocations("/js/").setCachePeriod(64000);
        registry.addResourceHandler("/css/**").addResourceLocations("/css/").setCachePeriod(64000);
        registry.addResourceHandler("/static/img/**").addResourceLocations("/static/img/").setCachePeriod(64000);
        registry.addResourceHandler("/docs/**").addResourceLocations("/docs/").setCachePeriod(64000);
        registry.addResourceHandler("/admindocs/**").addResourceLocations("/admin-docs/").setCachePeriod(64000);
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/static/img/icons/favicon.ico").setCachePeriod(64000);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
                .favorParameter(true).ignoreAcceptHeader(true);
    }

    /**
     * This view resolver will map view names returned from the controllers to jsp files stored in the
     * configured 'prefix' url.
     */
    @Bean(name = "viewResolver")
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        return viewResolver;
    }

    /**
     * Sets a fixed locale for the whole application.
     */
    @Bean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("en-US"));
    }
}
