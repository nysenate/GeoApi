package gov.nysenate.sage.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import gov.nysenate.sage.factory.SageThreadFactory;
import gov.nysenate.sage.model.notification.Notification;
import gov.nysenate.sage.util.ExecutorUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
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
public class ApplicationConfig implements SchedulingConfigurer, AsyncConfigurer
{
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    /** --- Eh Cache Spring Configuration --- */

    @Value("${validate.threads:3}") private int validateThreads;

    @Value("${distassign.threads:3}") private int distassignThreads;

    @Value("${geocode.threads:3}") private int geocodeThreads;

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
        return ExecutorUtil.createExecutor("spring-async", 10);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Bean(name = "jobValidator", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor getJobAddressValidationExecutor() {
        return ExecutorUtil.createExecutor("job-validator", validateThreads);
    }

    @Bean(name = "jobGeocoder", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor getJobGeocodeExecutor() {
        return ExecutorUtil.createExecutor("job-geocoder", geocodeThreads);
    }

    @Bean(name = "jobDistAssign", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor getJobDistrictAssignExecutor() {
        return ExecutorUtil.createExecutor("job-dist-assign", distassignThreads);
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
