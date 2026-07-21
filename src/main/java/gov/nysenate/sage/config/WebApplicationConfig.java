package gov.nysenate.sage.config;

import gov.nysenate.sage.dao.logger.deployment.SqlDeploymentLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
        CacheControl oneDay = CacheControl.maxAge(1, TimeUnit.DAYS);
        // The React entry point must always be revalidated: it points at the current
        // content-hashed bundle, so caching it would serve a stale app after deploys.
        registry.addResourceHandler("/static/dist/index.html")
                .addResourceLocations("/static/dist/index.html")
                .setCacheControl(CacheControl.noCache());
        // The bundle filename contains a content hash (see webpack.config.js), so it
        // can be cached indefinitely; a new build gets a new URL.
        registry.addResourceHandler("/static/dist/**").addResourceLocations("/static/dist/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        registry.addResourceHandler("/css/**").addResourceLocations("/css/").setCacheControl(oneDay);
        // Serves /static/img and the other unhashed static assets.
        registry.addResourceHandler("/static/**").addResourceLocations("/static/").setCacheControl(oneDay);
        registry.addResourceHandler("/docs/**").addResourceLocations("/docs/").setCacheControl(oneDay);
        // The admin docs share the nature theme assets with the regular docs, so only one
        // physical copy of _static is committed. See bin/build-docs.sh.
        registry.addResourceHandler("/admindocs/html/_static/**").addResourceLocations("/docs/html/_static/").setCacheControl(oneDay);
        registry.addResourceHandler("/admindocs/**").addResourceLocations("/admin-docs/").setCacheControl(oneDay);
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/static/img/icons/favicon.ico").setCacheControl(oneDay);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
                .favorParameter(true).ignoreAcceptHeader(true);
    }

    /**
     * Sets a fixed locale for the whole application.
     */
    @Bean(name = "localeResolver")
    public LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("en-US"));
    }
}
