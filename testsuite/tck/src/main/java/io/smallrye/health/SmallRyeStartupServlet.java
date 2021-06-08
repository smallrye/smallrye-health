package io.smallrye.health;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "SmallRyeStartupServlet", urlPatterns = "/health/started")
public class SmallRyeStartupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        SmallRyeHealth health = reporter.getStartup();
        if (health.isDown()) {
            resp.setStatus(503);
        }
        try {
            reporter.reportHealth(resp.getOutputStream(), health);
        } catch (IOException ioe) {
            HealthLogging.logger.error(ioe);
            resp.setStatus(500);
        }
    }

    @Inject
    private SmallRyeHealthReporter reporter;
}
