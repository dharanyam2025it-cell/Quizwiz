package com.example.QuizWiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttemptStartRequest {
    private Long studentId;
    private Long quizId;
}