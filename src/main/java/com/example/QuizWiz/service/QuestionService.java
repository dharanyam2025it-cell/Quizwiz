package com.example.QuizWiz.service;

import com.example.QuizWiz.dto.QuestionCreateRequest;
import com.example.QuizWiz.dto.QuestionResponse;
import com.example.QuizWiz.entity.Question;
import com.example.QuizWiz.entity.Quiz;
import com.example.QuizWiz.exception.ResourceNotFoundException;
import com.example.QuizWiz.repository.AttemptRepository;
import com.example.QuizWiz.repository.QuestionRepository;
import com.example.QuizWiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private static final Set<String> VALID_OPTIONS = Set.of("A", "B", "C", "D");

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;

    public QuestionResponse addQuestion(Long quizId, Long facultyId, QuestionCreateRequest request) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (facultyId == null || !quiz.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("You can modify only your own quizzes");
        }

        blockIfAttempted(quizId);

        Question question = new Question();
        fillQuestion(question, request);
        question.setQuiz(quiz);

        return toResponse(questionRepository.save(question));
    }

    public QuestionResponse updateQuestion(Long quizId, Long questionId,
                                           Long facultyId, QuestionCreateRequest request) {

        Question question = findOwnedQuestion(quizId, questionId, facultyId);
        blockIfAttempted(quizId);

        fillQuestion(question, request);

        return toResponse(questionRepository.save(question));
    }

    public void deleteQuestion(Long quizId, Long questionId, Long facultyId) {

        Question question = findOwnedQuestion(quizId, questionId, facultyId);
        blockIfAttempted(quizId);

        questionRepository.delete(question);
    }

    public List<QuestionResponse> getQuestionsByQuiz(Long quizId, Long facultyId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (facultyId == null || !quiz.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("You can view only your own quizzes");
        }

        return questionRepository.findByQuizId(quizId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- HELPERS ----------
    private Question findOwnedQuestion(Long quizId, Long questionId, Long facultyId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (!question.getQuiz().getId().equals(quizId)) {
            throw new ResourceNotFoundException("Question not found in this quiz");
        }

        if (facultyId == null || !question.getQuiz().getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("You can modify only your own quizzes");
        }
        return question;
    }

    private void blockIfAttempted(Long quizId) {
        if (attemptRepository.existsByQuizId(quizId)) {
            throw new IllegalArgumentException(
                    "Questions cannot be changed after students have attempted this quiz");
        }
    }

    private void fillQuestion(Question question, QuestionCreateRequest request) {

        requireText(request.getQuestionText(), "Question text is required");
        requireText(request.getOptionA(), "Option A is required");
        requireText(request.getOptionB(), "Option B is required");
        requireText(request.getOptionC(), "Option C is required");
        requireText(request.getOptionD(), "Option D is required");
        requireText(request.getCorrectOption(), "Correct option is required");

        String correct = request.getCorrectOption().trim().toUpperCase();
        if (!VALID_OPTIONS.contains(correct)) {
            throw new IllegalArgumentException("Correct option must be A, B, C or D");
        }

        if (request.getMarks() == null || request.getMarks() <= 0) {
            throw new IllegalArgumentException("Marks must be greater than 0");
        }

        question.setQuestionText(request.getQuestionText().trim());
        question.setOptionA(request.getOptionA().trim());
        question.setOptionB(request.getOptionB().trim());
        question.setOptionC(request.getOptionC().trim());
        question.setOptionD(request.getOptionD().trim());
        question.setCorrectOption(correct);
        question.setMarks(request.getMarks());
    }

    private void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private QuestionResponse toResponse(Question q) {
        return new QuestionResponse(
                q.getId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectOption(),
                q.getMarks(),
                q.getQuiz().getId()
        );
    }
}