package com.example.QuizWiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AttemptReviewResponse {
    private AttemptResultResponse result;
    private List<AnswerReviewItem> answers;
}