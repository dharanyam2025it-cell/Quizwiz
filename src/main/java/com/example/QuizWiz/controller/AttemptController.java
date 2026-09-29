package com.example.QuizWiz.controller;

import com.example.QuizWiz.dto.*;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @PostMapping("/start")
    public ResponseEntity<AttemptStartResponse> start(@RequestBody AttemptStartRequest request,
                                                      @AuthenticationPrincipal AuthUser user) {
        request.setStudentId(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(attemptService.startAttempt(request));
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<AttemptResultResponse> submit(@PathVariable Long attemptId,
                                                        @RequestBody AttemptSubmitRequest request,
                                                        @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(attemptService.submitAttempt(attemptId, user.getId(), request));
    }

    @GetMapping("/{attemptId}/review")
    public ResponseEntity<AttemptReviewResponse> review(@PathVariable Long attemptId,
                                                        @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(attemptService.getReview(attemptId, user.getId()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AttemptResultResponse>> myAttempts(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(attemptService.getAttemptsByStudent(user.getId()));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<AttemptResultResponse>> byQuiz(@PathVariable Long quizId,
                                                              @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(attemptService.getAttemptsByQuiz(quizId, user.getId()));
    }
}