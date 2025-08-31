package sample.project.Auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

public class CookiesFilter extends GenericFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String token = null;
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("accesstoken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token != null) {
            final String finalToken = token; // Make token final for use in the wrapper

            HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(req) {
                @Override
                public String getHeader(String name) {
                    if ("Authorization".equalsIgnoreCase(name)) {
                        return "Bearer " + finalToken;
                    }
                    return super.getHeader(name);
                }
            };
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}