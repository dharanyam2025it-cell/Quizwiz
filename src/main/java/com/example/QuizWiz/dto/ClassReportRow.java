package com.example.QuizWiz.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClassReportRow {
    private Long studentId;
    private String studentName;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private String status;
}