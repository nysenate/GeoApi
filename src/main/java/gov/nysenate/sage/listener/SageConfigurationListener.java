package gov.nysenate.sage.listener;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.event.ConfigurationErrorEvent;
import org.apache.commons.configuration.event.ConfigurationErrorListener;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.util.Observable;

/**
 * SageConfigurationListener listens to any changes to property files that have been loaded with
 * the {@link org.apache.commons.configuration.PropertiesConfiguration} class.
 */
public class SageConfigurationListener extends Observable
                                       implements ConfigurationListener, ConfigurationErrorListener {

    public Logger logger = Logger.getLogger(this.getClass());
    public SageConfigurationListener() {}

    @Override
    public void configurationChanged(ConfigurationEvent configurationEvent)
    {
        if (!configurationEvent.isBeforeUpdate())
        {
            logger.info(String.format("Configuration updated - notifying %d observers", this.countObservers()));
            setChanged();
            notifyObservers(this.getClass());
        }
    }

    @Override
    public void configurationError(ConfigurationErrorEvent configurationErrorEvent)
    {
        logger.error("A configuration error occurred!");
        logger.error(configurationErrorEvent.getCause().getMessage() + " " +
               configurationErrorEvent.getCause().getStackTrace());
    }
}
