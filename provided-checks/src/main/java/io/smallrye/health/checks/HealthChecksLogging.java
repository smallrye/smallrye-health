package io.smallrye.health.checks;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SRHCK", length = 5)
interface HealthChecksLogging extends BasicLogger {
    HealthChecksLogging log = Logger.getMessageLogger(HealthChecksLogging.class,
            HealthChecksLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2000, value = "Reporting Socket health check error ")
    void socketHealthCheckError(@Cause Throwable throwable);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2001, value = "Reporting InetAddress health check error ")
    void inetAddressHealthCheckError(@Cause Throwable throwable);

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 2002, value = "Reporting URL health check error ")
    void urlHealthCheckError(@Cause Throwable throwable);
}