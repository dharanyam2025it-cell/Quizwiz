package com.example.QuizWiz.dto;

import com.example.QuizWiz.dto.AnswerSubmitItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AttemptSubmitRequest {
    private List<AnswerSubmitItem> answers;
    private Boolean autoSubmit;
}