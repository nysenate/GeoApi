package gov.nysenate.sage.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PropertyConfig.class, DatabaseConfig.class, ApplicationConfig.class})

@ComponentScan(
        basePackages = "gov.nysenate.sage",
        excludeFilters = {
                @ComponentScan.Filter(value = WebApplicationConfig.class, type = FilterType.ASSIGNABLE_TYPE),
//                @ComponentScan.Filter(...),
        }
)
public class ConsoleApplicationConfig
{
    /** --- Any console specific bean definitions go here --- */
}
