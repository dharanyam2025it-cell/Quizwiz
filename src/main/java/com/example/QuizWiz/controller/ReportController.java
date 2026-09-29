package com.example.QuizWiz.controller;

import com.example.QuizWiz.dto.ClassReportResponse;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ClassReportResponse> classReport(@PathVariable Long quizId,
                                                           @RequestParam String className,
                                                           @AuthenticationPrincipal AuthUser user) {
        return ResponseEntity.ok(reportService.getClassReport(quizId, className, user.getId()));
    }
}