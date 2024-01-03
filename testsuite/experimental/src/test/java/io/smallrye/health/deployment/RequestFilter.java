package io.smallrye.health.deployment;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = "*", filterName = "ContextInfoFilter")
public class RequestFilter extends HttpFilter {

    public static final String TEST_CONTEXT_HEADER = "X-CONTEXT";

    @Inject
    ContextInfo contextInfo;

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        var value = req.getHeader(TEST_CONTEXT_HEADER);
        contextInfo.setValue(value);
        super.doFilter(req, res, chain);
    }

}
