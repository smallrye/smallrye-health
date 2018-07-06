package io.smallrye.health;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
@WebServlet(name = "SmallRyeHealthServlet", urlPatterns = "/*")
public class SmallRyeHealthServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        
        SmallRyeHealth health = reporter.getHealth();
        if (health.isDown()) {
            resp.setStatus(503);
        }
        reporter.reportHealth(resp.getOutputStream(), health);
    }

    @Inject
    private SmallRyeHealthReporter reporter;
}


