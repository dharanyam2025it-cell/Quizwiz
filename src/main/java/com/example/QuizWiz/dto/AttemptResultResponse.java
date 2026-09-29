package com.example.QuizWiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AttemptResultResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Long studentId;
    private String studentName;
    private String className;
    private Integer score;
    private Integer totalMarks;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
}