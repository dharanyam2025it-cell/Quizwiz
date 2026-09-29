package com.example.QuizWiz.web;

import com.example.QuizWiz.dto.AnswerSubmitItem;
import com.example.QuizWiz.dto.AttemptStartRequest;
import com.example.QuizWiz.dto.AttemptStartResponse;
import com.example.QuizWiz.dto.AttemptSubmitRequest;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.service.AttemptService;
import com.example.QuizWiz.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentPageController {

    private final QuizService quizService;
    private final AttemptService attemptService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AuthUser user, Model model) {
        model.addAttribute("availableQuizzes", quizService.getAvailableQuizzes(user.getId()));
        return "student/dashboard";

    }
    @PostMapping("/quizzes/{quizId}/start")
    public String startQuiz(@PathVariable Long quizId, @AuthenticationPrincipal AuthUser user) {

        AttemptStartRequest request = new AttemptStartRequest();
        request.setStudentId(user.getId());
        request.setQuizId(quizId);

        AttemptStartResponse response = attemptService.startAttempt(request);
        return "redirect:/student/attempts/" + response.getAttemptId() + "/take";
    }

    @GetMapping("/attempts/{attemptId}/take")
    public String takeAttempt(@PathVariable Long attemptId, Model model) {
        // NOTE: attemptId-ah already start pannina start response-ah namma store pannala,
        // adhanala simple-ah AttemptService-la irundhu edukka oru helper venum.
        model.addAttribute("start", attemptService.getStartInfo(attemptId));
        return "student/attempt-take";
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public String submitAttempt(@PathVariable Long attemptId,
                                @AuthenticationPrincipal AuthUser user,
                                HttpServletRequest httpRequest) {

        List<AnswerSubmitItem> answers = new ArrayList<>();

        httpRequest.getParameterMap().forEach((key, values) -> {
            if (key.startsWith("answer_") && values.length > 0 && !values[0].isBlank()) {
                Long questionId = Long.valueOf(key.substring("answer_".length()));
                AnswerSubmitItem item = new AnswerSubmitItem();
                item.setQuestionId(questionId);
                item.setSelectedOption(values[0]);
                answers.add(item);
            }
        });

        AttemptSubmitRequest request = new AttemptSubmitRequest();
        request.setAnswers(answers);
        request.setAutoSubmit("true".equals(httpRequest.getParameter("autoSubmit")));

        attemptService.submitAttempt(attemptId, user.getId(), request);
        return "redirect:/student/attempts/" + attemptId + "/result";
    }
    @GetMapping("/attempts/{attemptId}/result")
    public String resultPage(@PathVariable Long attemptId,
                             @AuthenticationPrincipal AuthUser user,
                             Model model) {
        model.addAttribute("review", attemptService.getReview(attemptId, user.getId()));
        return "student/attempt-result";
    }

    @GetMapping("/attempts")
    public String myAttempts(@AuthenticationPrincipal AuthUser user, Model model) {
        model.addAttribute("attempts", attemptService.getAttemptsByStudent(user.getId()));
        return "student/attempts-history";
    }
}