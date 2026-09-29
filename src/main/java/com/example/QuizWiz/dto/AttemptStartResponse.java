package com.example.QuizWiz.dto;

import com.example.QuizWiz.dto.StudentQuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AttemptStartResponse {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private Integer timeLimit;
    private LocalDateTime startTime;
    private List<StudentQuestionResponse> questions;
}