package org.foo.modules.directivefilter;

import org.osgi.service.component.annotations.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

@Component(service = {Filter.class}, property = {"pattern=/graphql"}, immediate = true)
public class QueryFilter implements Filter {

    // Detect strings such as @lala@lala@lala... and with space(s) in between @lala @lala
    private Pattern regex = Pattern.compile("(@[^ @]+[ ]*){10}");
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletResponse instanceof HttpServletResponse && servletRequest instanceof HttpServletRequest) {
            MultiReadRequestWrapper r = new MultiReadRequestWrapper((HttpServletRequest) servletRequest);
            String query = r.getReader().lines().collect(joining(" "));

            if (regex.matcher(query).find()) {
                HttpServletResponse resp = (HttpServletResponse) servletResponse;
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("{\"message\": \"You can only use up to 10 consecutive directives\"}");
                resp.getWriter().flush();
                return;
            }

            filterChain.doFilter(r, servletResponse);
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
