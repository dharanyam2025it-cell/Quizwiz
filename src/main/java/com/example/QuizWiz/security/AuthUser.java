package com.example.QuizWiz.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class AuthUser implements Serializable {
    private Long id;
    private String email;
    private String name;
    private String role;   // "FACULTY" or "STUDENT"
}