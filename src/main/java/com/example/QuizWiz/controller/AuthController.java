package com.example.QuizWiz.controller;

import com.example.QuizWiz.security.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<AuthUser> me(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(user);
    }
}