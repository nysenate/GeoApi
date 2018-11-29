package gov.nysenate.sage.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.notification.Notification;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SizeOfPolicyConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.LocalDateTime;

import static gov.nysenate.sage.model.notification.NotificationType.EVENT_BUS_EXCEPTION;

@Configuration
@EnableCaching
public class ApplicationConfig implements CachingConfigurer, SchedulingConfigurer, AsyncConfigurer
{
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    /** --- Eh Cache Spring Configuration --- */

    @Value("${cache.max.size}") private String cacheMaxHeapSize;

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager pooledCacheManger() {
        // Set the upper limit when computing heap size for objects. Once it reaches the limit
        // it stops computing further. Some objects can contain many references so we set the limit
        // fairly high.
        SizeOfPolicyConfiguration sizeOfConfig = new SizeOfPolicyConfiguration();
        sizeOfConfig.setMaxDepth(100000);
        sizeOfConfig.setMaxDepthExceededBehavior("continue");

        // Configure the default cache to be used as a template for actual caches.
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
        cacheConfiguration.addSizeOfPolicy(sizeOfConfig);

        // Configure the cache manager.
        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.setMaxBytesLocalHeap(cacheMaxHeapSize + "M");
        config.addDefaultCache(cacheConfiguration);
        config.setUpdateCheck(false);

        return net.sf.ehcache.CacheManager.newInstance(config);
    }

    @Override
    @Bean
    public CacheManager cacheManager() {
        return new EhCacheCacheManager(pooledCacheManger());
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver(cacheManager());
    }

    @Override
    @Bean
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }

    /** --- Guava Event Bus Configuration --- */

    @Bean
    public EventBus eventBus() {
        return new EventBus(this::handleEventBusException);
    }

    @Bean
    public AsyncEventBus asyncEventBus() {
        return new AsyncEventBus(getAsyncExecutor(), this::handleEventBusException);
    }

    /* --- Threadpool/Async/Scheduling Configuration --- */

    @Bean(name = "taskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler getTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadFactory(new SageThreadFactory("scheduler"));
        scheduler.setPoolSize(8);
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(getTaskScheduler());
    }

    @Override
    @Bean(name = "sageAsync", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadFactory(new SageThreadFactory("spring-async"));
        executor.setCorePoolSize(10);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    /**
     * Handle event bus exceptions by posting a notification.
     *
     * Note that even though notifications are posted through the event bus,
     * all exceptions are caught within the notification event handling code, preventing an infinite loop.
     *
     * @param exception Throwable
     * @param context SubscriberExceptionContext
     */
    private void handleEventBusException(Throwable exception, SubscriberExceptionContext context) {
        logger.error("Event Bus Exception thrown during event handling within " + context.getSubscriberMethod(), exception);

        LocalDateTime occurred = LocalDateTime.now();
        String summary = "Event Bus Exception within " + context.getSubscriberMethod() +
                " at " + occurred + " - " + ExceptionUtils.getStackFrames(exception)[0];
        String message = "\nThe following exception occurred during event handling within " +
                context.getSubscriberMethod() + " at " + occurred + ":\n" +
                ExceptionUtils.getStackTrace(exception);
        Notification notification = new Notification(EVENT_BUS_EXCEPTION, occurred, summary, message);

        eventBus().post(notification);
    }
}
