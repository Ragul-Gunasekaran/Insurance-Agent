package com.ubi.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubi.platform.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.*;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Spring Security 6 Configuration for Usage-Based Insurance Platform (Step 8).
 * Configures session-based cookie authentication, CSRF protections for SPAs,
 * role-based access control (CUSTOMER vs INSURER), and explicit public vs. protected routes.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${server.servlet.session.cookie.same-site:Lax}")
    private String cookieSameSite;

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(customizer -> {
            if (cookieSecure) {
                customizer.secure(true);
            }
            if (cookieSameSite != null && !cookieSameSite.isBlank()) {
                customizer.sameSite(cookieSameSite);
            }
        });
        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                .ignoringRequestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/logout",
                    "/api/v1/auth/csrf",
                    "/api/v1/health",
                    "/api/v1/info",
                    "/actuator/**"
                )
            )
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().migrateSession()
                .maximumSessions(5)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("Unauthorized access attempt to {}: {}", request.getRequestURI(), authException.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<?> apiResponse = ApiResponse.fail(
                        "UNAUTHORIZED",
                        "Authentication is required to access this resource",
                        HttpServletResponse.SC_UNAUTHORIZED,
                        request.getRequestURI()
                    );
                    objectMapper.writeValue(response.getOutputStream(), apiResponse);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("Access denied to {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<?> apiResponse = ApiResponse.fail(
                        "FORBIDDEN",
                        "Access denied: insufficient role privileges for this resource",
                        HttpServletResponse.SC_FORBIDDEN,
                        request.getRequestURI()
                    );
                    objectMapper.writeValue(response.getOutputStream(), apiResponse);
                })
            )
            .authorizeHttpRequests(auth -> auth
                // Public authentication and operational endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/logout").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/auth/csrf", "/api/v1/health", "/api/v1/info", "/actuator/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/pricing/configuration").permitAll()

                // Insurer-only fleet underwriting dashboard endpoints
                .requestMatchers("/api/v1/dashboard/insurer/**").hasRole("INSURER")

                // Authenticated user profile endpoint
                .requestMatchers("/api/v1/auth/me").authenticated()

                // All other business endpoints require authentication
                .requestMatchers("/api/v1/**").authenticated()

                // Deny any other unspecified routes by default
                .anyRequest().denyAll()
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Origin",
                "X-XSRF-TOKEN"
        ));
        config.setExposedHeaders(List.of("X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * SPA CSRF Token Request Handler supporting raw and XOR token values.
     */
    static final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
            this.delegate.handle(request, response, csrfToken);
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            String headerValue = request.getHeader(csrfToken.getHeaderName());
            return (StringUtils.hasText(headerValue)) ? headerValue : this.delegate.resolveCsrfTokenValue(request, csrfToken);
        }
    }

    /**
     * Filter that ensures the CSRF token cookie is committed on every request.
     */
    static final class CsrfCookieFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                // Invoking getToken() forces token evaluation and cookie generation
                csrfToken.getToken();
            }
            filterChain.doFilter(request, response);
        }
    }
}
