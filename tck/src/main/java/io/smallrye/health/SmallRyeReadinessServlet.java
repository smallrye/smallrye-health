package io.smallrye.health;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@SuppressWarnings("serial")
@WebServlet(name = "SmallRyeReadinessServlet", urlPatterns = "/health/ready")
public class SmallRyeReadinessServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        SmallRyeHealth health = reporter.getReadiness();
        if (health.isDown()) {
            resp.setStatus(503);
        }
        reporter.reportHealth(resp.getOutputStream(), health);
    }

    @Inject
    private SmallRyeHealthReporter reporter;
}


