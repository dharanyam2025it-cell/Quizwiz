package com.example.QuizWiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClassReportResponse {
    private Long quizId;
    private String quizTitle;
    private String className;
    private Integer totalStudents;
    private Integer submittedCount;
    private Double averageScore;
    private Integer highestScore;
    private Integer lowestScore;
    private List<ClassReportRow> students;
}