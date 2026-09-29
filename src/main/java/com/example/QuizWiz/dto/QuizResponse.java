package com.example.QuizWiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class QuizResponse {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimit;
    private LocalDateTime createdAt;
    private Long facultyId;
    private String facultyName;
}