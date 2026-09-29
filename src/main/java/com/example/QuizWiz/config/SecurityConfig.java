package com.example.QuizWiz.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String path = request.getRequestURI();
                            if (path.startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            } else {
                                response.sendRedirect("/login");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/login"))
                .authorizeHttpRequests(auth -> auth

                        // Public pages
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/error").permitAll()

                        // Public REST APIs
                        .requestMatchers("/api/faculty/register", "/api/faculty/login",
                                "/api/student/register", "/api/student/login").permitAll()

                        // Faculty only
                        .requestMatchers("/faculty/**").hasRole("FACULTY")
                        .requestMatchers("/api/quizzes/*/questions/**").hasRole("FACULTY")
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/my").hasRole("FACULTY")
                        .requestMatchers(HttpMethod.POST, "/api/quizzes").hasRole("FACULTY")
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/*").hasRole("FACULTY")
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/*").hasRole("FACULTY")
                        .requestMatchers("/api/reports/**").hasRole("FACULTY")
                        .requestMatchers(HttpMethod.GET, "/api/attempts/quiz/*").hasRole("FACULTY")

                        // Student only
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/attempts/start",
                                "/api/attempts/*/submit").hasRole("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/attempts/my",
                                "/api/attempts/*/review").hasRole("STUDENT")

                        // Any logged-in user
                        .requestMatchers(HttpMethod.GET, "/api/quizzes", "/api/quizzes/*").authenticated()
                        .requestMatchers("/api/auth/me").authenticated()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}