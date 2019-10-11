package gov.nysenate.sage.config;

import gov.nysenate.sage.dao.logger.deployment.SqlDeploymentLogger;
import gov.nysenate.sage.util.AsciiArt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebMvc
@EnableAsync
@EnableScheduling
@ComponentScan("gov.nysenate.sage")
@Import({DatabaseConfig.class, SecurityConfig.class, ApplicationConfig.class })
public class WebApplicationConfig implements WebMvcConfigurer
{
    private static final Logger logger = LogManager.getLogger(WebApplicationConfig.class);

    @Autowired
    SqlDeploymentLogger sqlDeploymentLogger;

    @PostConstruct
    public void init() {
        LocalDateTime deployTime = LocalDateTime.now();
        Timestamp timestamp = java.sql.Timestamp.valueOf(deployTime);
        logger.info("{}", AsciiArt.SAGE.getText().replace("DATE", deployTime.toString()));
        sqlDeploymentLogger.logDeploymentStatus(true,-1,timestamp);
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

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        stringConverter.setWriteAcceptCharset(false);
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(stringConverter);
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new SourceHttpMessageConverter<Source>());
        converters.add(new AllEncompassingFormHttpMessageConverter());
        converters.add(new Jaxb2RootElementHttpMessageConverter());
        //converters.add(jackson2Converter());
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.favorPathExtension(false);
    }
}
