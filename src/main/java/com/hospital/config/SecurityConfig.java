package com.hospital.config;

import com.hospital.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Central security wiring.
 *
 *  - Stateless: no server session; every request authenticates via the JWT filter.
 *  - Public endpoints: login, the API docs.
 *  - register: ADMIN only.
 *  - Everything else: must be authenticated; fine-grained role/data checks live in
 *    the service layer and via @PreAuthorize (enabled by @EnableMethodSecurity).
 */
@Configuration
@EnableMethodSecurity   // turns on @PreAuthorize("hasRole('ADMIN')") etc.
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationEntryPoint authEntryPoint;   // 401 as JSON
    private final AccessDeniedHandler accessDeniedHandler;   // 403 as JSON
    private final String allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          AuthenticationEntryPoint authEntryPoint,
                          AccessDeniedHandler accessDeniedHandler,
                          @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins}") String allowedOrigins) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.allowedOrigins = allowedOrigins;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // We're a stateless JSON API using JWTs, so CSRF protection (which targets
            // cookie/session based browser forms) is not applicable.
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/auth/login",
                            "/api/health",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/h2-console/**"
                    ).permitAll()
                    .requestMatchers("/api/auth/register").hasRole("ADMIN")
                    .anyRequest().authenticated()
            )
            // Return clean JSON for auth failures instead of HTML login pages.
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            // H2 console renders in a frame; allow same-origin frames in dev.
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Our JWT filter runs before the username/password filter.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt: slow, salted password hashing. Used to hash on register, verify on login. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Wires our UserDetailsService + password encoder for the login flow. */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService uds,
                                                            PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /** Allow the React dev server(s) to call this API from the browser. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
