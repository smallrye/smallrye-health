package io.smallrye.health;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "SmallRyeLivenessServlet", urlPatterns = "/health/live")
public class SmallRyeLivenessServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        SmallRyeHealth health = reporter.getLiveness();
        if (health.isDown()) {
            resp.setStatus(503);
        }
        try {
            reporter.reportHealth(resp.getOutputStream(), health);
        } catch (IOException ioe) {
            HealthLogging.log.error(ioe);
            throw new RuntimeException(ioe);
        }
    }

    @Inject
    private SmallRyeHealthReporter reporter;
}
