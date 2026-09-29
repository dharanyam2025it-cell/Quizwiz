package com.example.QuizWiz.service;

import com.example.QuizWiz.dto.*;
import com.example.QuizWiz.entity.*;
import com.example.QuizWiz.exception.ResourceNotFoundException;
import com.example.QuizWiz.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String SUBMITTED = "SUBMITTED";
    private static final String AUTO_SUBMITTED = "AUTO_SUBMITTED";
    private static final long GRACE_SECONDS = 10;
    private static final Set<String> VALID_OPTIONS = Set.of("A", "B", "C", "D");

    private final AttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final StudentRepository studentRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    // ---------- START ----------
    public AttemptStartResponse startAttempt(AttemptStartRequest request) {

        if (request.getStudentId() == null || request.getQuizId() == null) {
            throw new IllegalArgumentException("Student id and quiz id are required");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("This quiz has no questions yet");
        }

        if (attemptRepository.
                existsByStudentIdAndQuizId(student.getId(), quiz.getId())) {
            throw new IllegalArgumentException("You have already attempted this quiz");
        }

        Attempt attempt = new Attempt();
        attempt.setStudent(student);
        attempt.setQuiz(quiz);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setStatus(IN_PROGRESS);
        attempt = attemptRepository.save(attempt);

        List<StudentQuestionResponse> questionDtos = questions.stream()
                .map(this::toStudentQuestion)
                .toList();

        return new AttemptStartResponse(
                attempt.getId(),
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTimeLimit(),
                attempt.getStartTime(),
                questionDtos
        );
    }
    public AttemptStartResponse getStartInfo(Long attemptId) {

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (!IN_PROGRESS.equals(attempt.getStatus())) {
            throw new IllegalArgumentException("This attempt is already submitted");
        }

        List<Question> questions = questionRepository.findByQuizId(attempt.getQuiz().getId());

        List<StudentQuestionResponse> questionDtos = questions.stream()
                .map(this::toStudentQuestion)
                .toList();

        return new AttemptStartResponse(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getQuiz().getTimeLimit(),
                attempt.getStartTime(),
                questionDtos
        );
    }

    // ---------- SUBMIT ----------
    @Transactional
    public AttemptResultResponse submitAttempt(Long attemptId, Long studentId, AttemptSubmitRequest request) {

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (studentId == null || !attempt.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("This attempt does not belong to you");
        }

        if (!IN_PROGRESS.equals(attempt.getStatus())) {
            throw new IllegalArgumentException("This attempt is already submitted");
        }
        // ... baaki method munnadhi maadhiriye

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = attempt.getStartTime().plusMinutes(attempt.getQuiz().getTimeLimit());
        boolean tooLate = now.isAfter(deadline.plusSeconds(GRACE_SECONDS));

        List<Question> questions = questionRepository.findByQuizId(attempt.getQuiz().getId());
        int totalMarks = questions.stream().mapToInt(Question::getMarks).sum();

        // Time mudinja piragu vandha submit: answers count aagaadhu
        if (tooLate) {
            attempt.setSubmitTime(now);
            attempt.setScore(0);
            attempt.setTotalMarks(totalMarks);
            attempt.setStatus(AUTO_SUBMITTED);
            return toResult(attemptRepository.save(attempt));
        }

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<AnswerSubmitItem> items =
                request.getAnswers() == null ? List.of() : request.getAnswers();

        Set<Long> answeredIds = new HashSet<>();
        List<Answer> answers = new ArrayList<>();
        int score = 0;

        for (AnswerSubmitItem item : items) {

            Question question = questionMap.get(item.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException(
                        "Question " + item.getQuestionId() + " does not belong to this quiz");
            }
            if (!answeredIds.add(question.getId())) {
                throw new IllegalArgumentException(
                        "Duplicate answer for question " + question.getId());
            }

            String selected = null;
            if (item.getSelectedOption() != null && !item.getSelectedOption().isBlank()) {
                selected = item.getSelectedOption().trim().toUpperCase();
                if (!VALID_OPTIONS.contains(selected)) {
                    throw new IllegalArgumentException("Selected option must be A, B, C or D");
                }
            }

            boolean correct = selected != null && selected.equals(question.getCorrectOption());
            if (correct) {
                score += question.getMarks();
            }

            answers.add(buildAnswer(attempt, question, selected, correct));
        }

        // Skip pannina questions
        for (Question question : questions) {
            if (!answeredIds.contains(question.getId())) {
                answers.add(buildAnswer(attempt, question, null, false));
            }
        }

        answerRepository.saveAll(answers);

        attempt.setSubmitTime(now);
        attempt.setScore(score);
        attempt.setTotalMarks(totalMarks);
        attempt.setStatus(Boolean.TRUE.equals(request.getAutoSubmit()) ? AUTO_SUBMITTED : SUBMITTED);

        return toResult(attemptRepository.save(attempt));
    }

    // ---------- READ ----------
    public AttemptResultResponse getResult(Long attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        return toResult(attempt);
    }

    public List<AttemptResultResponse> getAttemptsByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found");
        }
        return attemptRepository.findByStudentId(studentId).stream().map(this::toResult).toList();
    }

    public List<AttemptResultResponse> getAttemptsByQuiz(Long quizId, Long facultyId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (facultyId == null || !quiz.getFaculty().getId().equals(facultyId)) {
            throw new IllegalArgumentException("You can view only your own quizzes");
        }

        return attemptRepository.findByQuizId(quizId).stream().map(this::toResult).toList();
    }

    public AttemptReviewResponse getReview(Long attemptId, Long studentId) {

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (studentId == null || !attempt.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("This attempt does not belong to you");
        }

        if (IN_PROGRESS.equals(attempt.getStatus())) {
            throw new IllegalArgumentException("Submit the quiz to see the review");
        }

        List<Question> questions = questionRepository.findByQuizId(attempt.getQuiz().getId());

        Map<Long, Answer> answerMap = answerRepository.findByAttemptId(attemptId).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (a, b) -> a));

        List<AnswerReviewItem> items = questions.stream()
                .sorted(Comparator.comparing(Question::getId))
                .map(q -> {
                    Answer answer = answerMap.get(q.getId());
                    String selected = answer == null ? null : answer.getSelectedOption();
                    boolean correct = answer != null && Boolean.TRUE.equals(answer.getCorrect());
                    return new AnswerReviewItem(
                            q.getId(), q.getQuestionText(),
                            q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                            selected, q.getCorrectOption(), correct,
                            q.getMarks(), correct ? q.getMarks() : 0
                    );
                })
                .toList();

        return new AttemptReviewResponse(toResult(attempt), items);
    }

    // ---------- HELPERS ----------
    private Answer buildAnswer(Attempt attempt, Question question, String selected, boolean correct) {
        Answer answer = new Answer();
        answer.setAttempt(attempt);
        answer.setQuestion(question);
        answer.setSelectedOption(selected);
        answer.setCorrect(correct);
        return answer;
    }

    private StudentQuestionResponse toStudentQuestion(Question q) {
        return new StudentQuestionResponse(
                q.getId(), q.getQuestionText(),
                q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                q.getMarks()
        );
    }

    private AttemptResultResponse toResult(Attempt a) {
        return new AttemptResultResponse(
                a.getId(),
                a.getQuiz().getId(),
                a.getQuiz().getTitle(),
                a.getStudent().getId(),
                a.getStudent().getName(),
                a.getStudent().getClassName(),
                a.getScore(),
                a.getTotalMarks(),
                a.getStatus(),
                a.getStartTime(),
                a.getSubmitTime()
        );
    }
}