package org.ifarmr.config;

import lombok.RequiredArgsConstructor;
import org.ifarmr.controller.NotificationTokenController;

import org.ifarmr.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity security) throws Exception {
        security.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        security.addFilterAfter(new LastActiveFilter(userService), UsernamePasswordAuthenticationFilter.class);

        security.csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(
                        requests -> requests
                                .requestMatchers(
                                        antMatcher(HttpMethod.POST, "/api/v1/auth/**"),
                                        antMatcher(HttpMethod.POST, "/token/**"),
                                        antMatcher(HttpMethod.GET, "/api/v1/auth/**"),
                                        antMatcher(HttpMethod.GET, "/swagger-ui.html"),
                                        antMatcher(HttpMethod.GET, "/swagger-ui/**"),
                                        antMatcher(HttpMethod.GET, "/v3/api-docs/**"),
                                        antMatcher(HttpMethod.GET, "/swagger-resources/**")
                                ).permitAll()
                                .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/api/v1/user/**").hasAuthority("USER")
                                .requestMatchers("/api/v1/posts/**").hasAuthority("USER")
                                .requestMatchers("/api/v1/tasks/**").hasAuthority("USER")
                                .requestMatchers("/api/v1/crops/**").hasAuthority("USER")
                                .requestMatchers("/api/v1/inventory/**").hasAuthority("USER")
                                .requestMatchers("/api/v1/livestock/**").hasAuthority("USER")
                                .requestMatchers("/api/v1/notifications/**").hasAuthority("USER")
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider)
                .formLogin(form -> form
                        .failureHandler(customAuthenticationFailureHandler))
                .cors(customizer -> customizer.configurationSource(corsConfigurationSource()));

        // Add logging for debugging
        security.addFilterBefore((request, response, chain) -> {
            logger.info("Security configuration applied");
            chain.doFilter(request, response);
        }, UsernamePasswordAuthenticationFilter.class);

        return security.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
