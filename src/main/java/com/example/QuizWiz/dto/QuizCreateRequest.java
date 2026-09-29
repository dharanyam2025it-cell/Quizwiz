package com.example.QuizWiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuizCreateRequest {
    private Long facultyId;
    private String title;
    private String description;
    private Integer timeLimit;
}