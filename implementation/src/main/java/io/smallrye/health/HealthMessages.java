package io.smallrye.health;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SRHCK", length = 5)
interface HealthMessages {
    HealthMessages msg = Messages.getBundle(HealthMessages.class);

    @Message(id = 0, value = "Health Check contains an invalid name. Can not be null or empty.")
    IllegalArgumentException invalidHealthCheckName();

    @Message(id = 1, value = "Health Check returned null.")
    NullPointerException healthCheckNull();
}
