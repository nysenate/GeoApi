package gov.nysenate.sage.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import gov.nysenate.sage.controller.interceptor.PageSetupInterceptor;
import gov.nysenate.sage.dao.logger.deployment.SqlDeploymentLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private final PageSetupInterceptor pageSetupInterceptor;

    @Autowired
    public WebApplicationConfig(SqlDeploymentLogger sqlDeploymentLogger, PageSetupInterceptor pageSetupInterceptor) {
        this.sqlDeploymentLogger = sqlDeploymentLogger;
        this.pageSetupInterceptor = pageSetupInterceptor;
    }

    /** Populates the request attributes needed by the front-end map/lookup views. */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(pageSetupInterceptor)
                .addPathPatterns("/", "/admin/home", "/map", "/map/**");
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
        // The admin docs share the nature theme assets with the regular docs, so only one
        // physical copy of _static is committed. See bin/build-docs.sh.
        registry.addResourceHandler("/admindocs/html/_static/**").addResourceLocations("/docs/html/_static/").setCachePeriod(64000);
        registry.addResourceHandler("/admindocs/**").addResourceLocations("/admin-docs/").setCachePeriod(64000);
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/static/img/icons/").setCachePeriod(64000);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
                .favorParameter(true).ignoreAcceptHeader(true);
    }

    /**
     * By default, a Map.Entry is serialized with the key becoming the field name, which is
     * invalid XML whenever that key is numeric. Requesting the OBJECT shape instead makes
     * Jackson fall back to MapEntryAsPOJOSerializer, which writes plain key and value fields.
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (var converter : converters) {
            if (converter instanceof AbstractJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.getObjectMapper().configOverride(Map.Entry.class)
                        .setFormat(JsonFormat.Value.forShape(JsonFormat.Shape.OBJECT));
            }
        }
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
