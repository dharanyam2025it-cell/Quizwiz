package com.example.QuizWiz.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionAuthenticator {

    private final SecurityContextRepository repository = new HttpSessionSecurityContextRepository();

    public void login(AuthUser user, HttpServletRequest request, HttpServletResponse response) {

        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
                );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        repository.saveContext(context, request, response);
    }
}