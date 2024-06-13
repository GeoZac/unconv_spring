package com.unconv.spring.security.filter;

import static com.unconv.spring.consts.AppConstants.ACCESS_TOKEN;

import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWTAuthenticationFilter is a filter responsible for authenticating requests using JWT tokens. It
 * extends OncePerRequestFilter, ensuring that it is only executed once per request.
 */
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX_STRING = "Bearer ";

    private final JWTUtil jwtUtil;

    private final SensorAuthTokenUtil sensorAuthTokenUtil;

    @Value("${auth.skip-header:false}")
    private boolean skipAuthHeader;

    /**
     * Constructs a JWTAuthenticationFilter with the specified JWTUtil and SensorAuthTokenUtil.
     *
     * @param jwtUtil the utility class for JWT operations
     * @param sensorAuthTokenUtil the utility class for sensor authentication token operations
     */
    public JWTAuthenticationFilter(JWTUtil jwtUtil, SensorAuthTokenUtil sensorAuthTokenUtil) {
        this.jwtUtil = jwtUtil;
        this.sensorAuthTokenUtil = sensorAuthTokenUtil;
    }

    /**
     * Performs the actual filtering for a request.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain
     * @throws IOException if an I/O error occurs
     * @throws ServletException if an error occurs during servlet processing
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        String contextUser;

        if (isSensorPostingEnvironmentalReadingWithToken(request)) {
            String accessToken = request.getParameter(ACCESS_TOKEN);

            contextUser = sensorAuthTokenUtil.validateTokenAndRetrieveUser(accessToken);
            if (contextUser == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid API token");
                return;
            }
        } else if (header == null || !header.startsWith(BEARER_PREFIX_STRING) || skipAuthHeader) {
            filterChain.doFilter(request, response);
            return;
        } else {
            String token =
                    header.startsWith(BEARER_PREFIX_STRING)
                            ? header.replace(BEARER_PREFIX_STRING, "")
                            : header;
            contextUser = jwtUtil.validateTokenAndRetrieveSubject(token);
        }

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(contextUser, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the request is from a sensor posting environmental reading with a token.
     *
     * @param request the HTTP servlet request
     * @return true if the request is from a sensor posting environmental reading with a token,
     *     false otherwise
     */
    boolean isSensorPostingEnvironmentalReadingWithToken(HttpServletRequest request) {
        return "/EnvironmentalReading".equals(request.getRequestURI())
                && "POST".equals(request.getMethod())
                && request.getParameter(ACCESS_TOKEN) != null;
    }
}
