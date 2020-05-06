package io.smallrye.health;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SRHCK", length = 5)
interface HealthLogging extends BasicLogger {
    HealthLogging log = Logger.getMessageLogger(HealthLogging.class, HealthLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 1000, value = "Error processing Health Checks")
    void healthCheckError(@Cause Throwable throwable);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 1001, value = "Reporting health down status: %s")
    void healthDownStatus(String cause);
}