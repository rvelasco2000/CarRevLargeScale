package it.unipi.CarRev.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/error",
                                "/swagger-auth.js"
                        ).permitAll()
                        .requestMatchers(
                                "/api/general/**"
                        ).permitAll()
                        .requestMatchers(
                                "/api/user/**"
                        ).authenticated()
                        .requestMatchers(
                                "/api/test/scheduled/**"
                        ).permitAll() //for testing in real life application we will not have this
                        .requestMatchers(HttpMethod.GET, "/api/test/neo4j/ping").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/test/neo4j/car").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/test/neo4j/recommend").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/cars/logged/**").authenticated()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/cars",
                                "/api/cars/visitCar",
                                "/api/cars/mostLikedReviews",
                                "/api/cars/otherReviews").permitAll()
                        .requestMatchers("/api/cars/logged/lastFive").authenticated()

                        .anyRequest().authenticated()
                )
               .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
