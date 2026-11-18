package com.jcaa.usersmanagement.infrastructure.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

  private static final String AUTH_LOGIN_PATH = "/api/auth/login";
  private static final String USERS_PATH = "/api/users";
  private static final String USERS_FILES_PATH = "/api/users/*/files";
  private static final String USERS_FILES_DETAIL_PATH = "/api/users/*/files/**";
  private static final String USERS_DETAIL_PATH = "/api/users/**";
  private static final String SEARCH_USERS_PATH = "/api/search/users";
  private static final String SEARCH_USERS_REINDEX_PATH = "/api/search/users/reindex";
  private static final String[] OPEN_API_PATHS = {
    "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(
      final HttpSecurity http,
      final JwtAuthenticationFilter jwtAuthenticationFilter,
      final RestAuthenticationEntryPoint authenticationEntryPoint,
      final RestAccessDeniedHandler accessDeniedHandler)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            handling ->
                handling
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
        .authorizeHttpRequests(
            authorization ->
                authorization
                    .requestMatchers(AUTH_LOGIN_PATH)
                    .permitAll()
                    .requestMatchers(OPEN_API_PATHS)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, USERS_PATH)
                    .permitAll()
                    .requestMatchers(USERS_FILES_PATH, USERS_FILES_DETAIL_PATH)
                    .authenticated()
                    .requestMatchers(HttpMethod.POST, SEARCH_USERS_REINDEX_PATH)
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, SEARCH_USERS_PATH)
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, USERS_PATH, USERS_DETAIL_PATH)
                    .hasAnyRole("ADMIN", "REVIEWER")
                    .requestMatchers(HttpMethod.PUT, USERS_DETAIL_PATH)
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, USERS_DETAIL_PATH)
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
