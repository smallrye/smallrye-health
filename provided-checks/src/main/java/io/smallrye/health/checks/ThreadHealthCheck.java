package io.smallrye.health.checks;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

/**
 * Health check implementation that is checking the number of threads
 *
 * <pre>
 * {@code
 * &#64;Produces
 * &#64;ApplicationScoped
 * &#64;Liveness
 * HealthCheck check1() {
 *   return new ThreadHealthCheck(9999999L);
 * }
 * }
 * </pre>
 */
public class ThreadHealthCheck implements HealthCheck {

    long maxThreadCount;

    public ThreadHealthCheck(long maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    @Override
    public HealthCheckResponse call() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("threads").withData("max thread count",
                maxThreadCount);
        addThreadCount(responseBuilder, threadMXBean);
        addPeakThreadCount(responseBuilder, threadMXBean);
        addDaemonThreadCount(responseBuilder, threadMXBean);
        addTotalStartedThreadCount(responseBuilder, threadMXBean);
        addDeadlockedThreads(responseBuilder, threadMXBean);
        addMonitorDeadlockedThreads(responseBuilder, threadMXBean);

        addStatus(responseBuilder, threadMXBean);

        return responseBuilder.build();
    }

    private void addStatus(HealthCheckResponseBuilder responseBuilder, ThreadMXBean threadMXBean) {
        int threadCount = -1;

        try {
            threadCount = threadMXBean.getThreadCount();
        } catch (Throwable t) {
            // Can not get thread count
        }
        if (threadCount > 0 && maxThreadCount > 0) {
            boolean status = threadCount < maxThreadCount;
            responseBuilder.state(status);
        } else {
            // Thread count not available
            responseBuilder.up();
        }

    }

    private void addThreadCount(HealthCheckResponseBuilder responseBuilder, ThreadMXBean threadMXBean) {
        try {
            responseBuilder.withData(THREAD_COUNT, threadMXBean.getThreadCount());
        } catch (Throwable t) {
            responseBuilder.withData(THREAD_COUNT, UNAVAILABLE);
        }
    }

    private void addPeakThreadCount(HealthCheckResponseBuilder responseBuilder, ThreadMXBean threadMXBean) {
        try {
            responseBuilder.withData(PEAK_THREAD_COUNT, threadMXBean.getPeakThreadCount());
        } catch (Throwable t) {
            responseBuilder.withData(PEAK_THREAD_COUNT, UNAVAILABLE);
        }
    }

    private void addDaemonThreadCount(HealthCheckResponseBuilder responseBuilder, ThreadMXBean threadMXBean) {
        try {
            responseBuilder.withData(DAEMON_THREAD_COUNT, threadMXBean.getDaemonThreadCount());
        } catch (Throwable t) {
            responseBuilder.withData(DAEMON_THREAD_COUNT, UNAVAILABLE);
        }
    }

    private void addTotalStartedThreadCount(HealthCheckResponseBuilder responseBuilder, ThreadMXBean threadMXBean) {
        try {
            responseBuilder.withData(STARTED_THREAD_COUNT, threadMXBean.getTotalStartedThreadCount());
        } catch (Throwable t) {
            responseBuilder.withData(STARTED_THREAD_COUNT, UNAVAILABLE);
        }
    }

    private void addDeadlockedThreads(HealthCheckResponseBuilder responseBuilder, ThreadMXBean threadMXBean) {
        try {
            long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
            responseBuilder.withData(DEADLOCKED_THREAD_COUNT, getNumberOfThreads(deadlockedThreads));
        } catch (Throwable t) {
            responseBuilder.withData(DEADLOCKED_THREAD_COUNT, UNAVAILABLE);
        }
    }

    private void addMonitorDeadlockedThreads(HealthCheckResponseBuilder responseBuilder, ThreadMXBean threadMXBean) {
        try {
            long[] monitorDeadlockedThreads = threadMXBean.findMonitorDeadlockedThreads();
            responseBuilder.withData(MONITOR_DEADLOCKED_THREAD_COUNT, getNumberOfThreads(monitorDeadlockedThreads));
        } catch (Throwable t) {
            responseBuilder.withData(MONITOR_DEADLOCKED_THREAD_COUNT, UNAVAILABLE);
        }
    }

    private int getNumberOfThreads(long[] ids) {
        if (ids == null)
            return 0;
        return ids.length;
    }

    private static final String THREAD_COUNT = "thread count";
    private static final String PEAK_THREAD_COUNT = "peak thread count";
    private static final String DAEMON_THREAD_COUNT = "daemon thread count";
    private static final String STARTED_THREAD_COUNT = "started thread count";
    private static final String DEADLOCKED_THREAD_COUNT = "deadlocked thread count";
    private static final String MONITOR_DEADLOCKED_THREAD_COUNT = "monitor deadlocked thread count";
    private static final String UNAVAILABLE = "Unavailable";
}
