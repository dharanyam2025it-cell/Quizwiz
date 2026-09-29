package com.example.QuizWiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FacultyResponse {
    private Long id;
    private String name;
    private String email;
}