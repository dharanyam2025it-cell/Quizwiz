package com.example.QuizWiz.service;

import com.example.QuizWiz.dto.QuizCreateRequest;
import com.example.QuizWiz.dto.QuizResponse;
import com.example.QuizWiz.dto.QuizUpdateRequest;
import com.example.QuizWiz.entity.Faculty;
import com.example.QuizWiz.entity.Quiz;
import com.example.QuizWiz.exception.ResourceNotFoundException;
import com.example.QuizWiz.repository.AttemptRepository;
import com.example.QuizWiz.repository.FacultyRepository;
import com.example.QuizWiz.repository.QuestionRepository;
import com.example.QuizWiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class QuizService {


    private final QuizRepository quizRepository;
    private final FacultyRepository facultyRepository;
    private final AttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;

    public QuizResponse createQuiz(QuizCreateRequest request) {

        if (request.getFacultyId() == null) {
            throw new IllegalArgumentException("Faculty id is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getTimeLimit() == null || request.getTimeLimit() <= 0) {
            throw new IllegalArgumentException("Time limit must be greater than 0");
        }

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle().trim());
        quiz.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setFaculty(faculty);

        return toResponse(quizRepository.save(quiz));
    }

    public List<QuizResponse> getQuizzesByFaculty(Long facultyId) {
        return quizRepository.findByFacultyId(facultyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public QuizResponse getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        return toResponse(quiz);
    }

    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QuizResponse> getAvailableQuizzes(Long studentId) {
        return quizRepository.findAll().stream()
                .filter(q -> !attemptRepository.existsByStudentIdAndQuizId(studentId, q.getId()))
                .map(this::toResponse)
                .toList();
    }

    public List<QuizResponse> getAttemptedQuizIds(Long studentId) {
        return attemptRepository.findByStudentId(studentId).stream()
                .map(a -> toResponse(a.getQuiz()))
                .toList();
    }

    private QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTimeLimit(),
                quiz.getCreatedAt(),
                quiz.getFaculty().getId(),
                quiz.getFaculty().getName()
        );
    }
    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizUpdateRequest request) {

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getTimeLimit() == null || request.getTimeLimit() <= 0) {
            throw new IllegalArgumentException("Time limit must be greater than 0");
        }

        Quiz quiz = findOwnedQuiz(quizId, request.getFacultyId());

        boolean timeChanged = !quiz.getTimeLimit().equals(request.getTimeLimit());
        if (timeChanged && attemptRepository.existsByQuizId(quizId)) {
            throw new IllegalArgumentException(
                    "Time limit cannot be changed after students have attempted this quiz");
        }

        quiz.setTitle(request.getTitle().trim());
        quiz.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        quiz.setTimeLimit(request.getTimeLimit());

        return toResponse(quizRepository.save(quiz));
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long facultyId) {

        Quiz quiz = findOwnedQuiz(quizId, facultyId);

        if (attemptRepository.existsByQuizId(quizId)) {
            throw new IllegalArgumentException(
                    "Cannot delete: students have already attempted this quiz");
        }

        questionRepository.deleteAll(questionRepository.findByQuizId(quizId));
        quizRepository.delete(quiz);
    }

    private Quiz findOwnedQuiz(Long quizId, Long facultyId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (facultyId == null || !quiz.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("You can modify only your own quizzes");
        }
        return quiz;
    }

}