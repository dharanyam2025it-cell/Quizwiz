package com.example.QuizWiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnswerSubmitItem {
    private Long questionId;
    private String selectedOption;
}