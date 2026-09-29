package com.example.QuizWiz.controller;

import com.example.QuizWiz.dto.QuestionCreateRequest;
import com.example.QuizWiz.dto.QuestionResponse;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionResponse> addQuestion(@PathVariable Long quizId,
                                                        @RequestBody QuestionCreateRequest request,
                                                        @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.addQuestion(quizId, user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestions(@PathVariable Long quizId,
                                                               @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(questionService.getQuestionsByQuiz(quizId, user.getId()));
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> update(@PathVariable Long quizId,
                                                   @PathVariable Long questionId,
                                                   @RequestBody QuestionCreateRequest request,
                                                   @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(
                questionService.updateQuestion(quizId, questionId, user.getId(), request));
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> delete(@PathVariable Long quizId,
                                       @PathVariable Long questionId,
                                       @AuthenticationPrincipal AuthUser user) {
        questionService.deleteQuestion(quizId, questionId, user.getId());
        return ResponseEntity.noContent().build();
    }
}