package io.smallrye.health;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "SmallRyeHealthGroupServlet", urlPatterns = "/health/group/*")
public class SmallRyeHealthGroupServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {

        String pathInfo = req.getPathInfo();
        SmallRyeHealth health = pathInfo != null ? reporter.getHealthGroup(pathInfo.substring(1)) : reporter.getHealthGroups();
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
