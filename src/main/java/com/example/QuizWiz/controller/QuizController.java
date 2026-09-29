package com.example.QuizWiz.controller;

import com.example.QuizWiz.dto.QuizCreateRequest;
import com.example.QuizWiz.dto.QuizResponse;
import com.example.QuizWiz.dto.QuizUpdateRequest;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(@RequestBody QuizCreateRequest request,
                                                   @AuthenticationPrincipal AuthUser user) {
        request.setFacultyId(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<QuizResponse>> myQuizzes(@AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(quizService.getQuizzesByFaculty(user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAll() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getById(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizById(quizId));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<QuizResponse> update(@PathVariable Long quizId,
                                               @RequestBody QuizUpdateRequest request,
                                               @AuthenticationPrincipal AuthUser user) {
        request.setFacultyId(user.getId());
        return ResponseEntity.ok(quizService.updateQuiz(quizId, request));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> delete(@PathVariable Long quizId,
                                       @AuthenticationPrincipal AuthUser user) {
        quizService.deleteQuiz(quizId, user.getId());
        return ResponseEntity.noContent().build();
    }
}