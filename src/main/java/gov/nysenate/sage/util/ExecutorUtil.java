package gov.nysenate.sage.util;

import gov.nysenate.sage.factory.SageThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public abstract class ExecutorUtil {

    public static ThreadPoolTaskExecutor createExecutor(String threadName, Integer poolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadFactory(new SageThreadFactory(threadName));
        executor.setCorePoolSize(poolSize);
        executor.initialize();
        return executor;
    }
}
