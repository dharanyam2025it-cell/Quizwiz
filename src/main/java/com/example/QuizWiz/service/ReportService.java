package com.example.QuizWiz.service;

import com.example.QuizWiz.dto.ClassReportResponse;
import com.example.QuizWiz.dto.ClassReportRow;
import com.example.QuizWiz.entity.Attempt;
import com.example.QuizWiz.entity.Quiz;
import com.example.QuizWiz.entity.Student;
import com.example.QuizWiz.exception.ResourceNotFoundException;
import com.example.QuizWiz.repository.AttemptRepository;
import com.example.QuizWiz.repository.QuizRepository;
import com.example.QuizWiz.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final String NOT_ATTEMPTED = "NOT_ATTEMPTED";

    private final AttemptRepository attemptRepository;
    private final StudentRepository studentRepository;
    private final QuizRepository quizRepository;

    public ClassReportResponse getClassReport(Long quizId, String className, Long facultyId) {

        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Class name is required");
        }

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (facultyId == null || !quiz.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("You can view reports only for your own quizzes");
        }
        // ... baaki method munnadhi maadhiriye

        List<Student> students = studentRepository.findByClassNameIgnoreCase(className.trim());
        if (students.isEmpty()) {
            throw new ResourceNotFoundException("No students found in this class");
        }

        Map<Long, Attempt> attemptByStudent = attemptRepository.findByQuizId(quizId).stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a, (a, b) -> a));

        List<ClassReportRow> rows = students.stream()
                .sorted(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER))
                .map(s -> toRow(s, attemptByStudent.get(s.getId())))
                .toList();

        List<Integer> scores = rows.stream()
                .map(ClassReportRow::getScore)
                .filter(sc -> sc != null)
                .toList();

        double average = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
        average = Math.round(average * 100.0) / 100.0;

        Integer highest = scores.stream().max(Integer::compare).orElse(null);
        Integer lowest = scores.stream().min(Integer::compare).orElse(null);

        return new ClassReportResponse(
                quiz.getId(),
                quiz.getTitle(),
                students.get(0).getClassName(),
                students.size(),
                scores.size(),
                average,
                highest,
                lowest,
                rows
        );
    }

    private ClassReportRow toRow(Student student, Attempt attempt) {

        if (attempt == null) {
            return new ClassReportRow(student.getId(), student.getName(),
                    null, null, null, NOT_ATTEMPTED);
        }

        Double percentage = null;
        if (attempt.getScore() != null && attempt.getTotalMarks() != null
                && attempt.getTotalMarks() > 0) {
            percentage = Math.round(attempt.getScore() * 10000.0 / attempt.getTotalMarks()) / 100.0;
        }

        return new ClassReportRow(student.getId(), student.getName(),
                attempt.getScore(), attempt.getTotalMarks(), percentage, attempt.getStatus());
    }
}