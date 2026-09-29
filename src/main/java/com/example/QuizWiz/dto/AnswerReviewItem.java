package com.example.QuizWiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerReviewItem {
    private Long questionId;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String selectedOption;
    private String correctOption;
    private Boolean correct;
    private Integer marks;
    private Integer marksAwarded;
}