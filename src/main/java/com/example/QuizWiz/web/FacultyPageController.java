package com.example.QuizWiz.web;

import com.example.QuizWiz.dto.ClassReportResponse;
import com.example.QuizWiz.dto.QuestionCreateRequest;
import com.example.QuizWiz.dto.QuizCreateRequest;
import com.example.QuizWiz.dto.QuizResponse;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.service.QuestionService;
import com.example.QuizWiz.service.QuizService;
import com.example.QuizWiz.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/faculty")
@RequiredArgsConstructor
public class FacultyPageController {

    private final ReportService reportService;
    private final QuizService quizService;
    private final QuestionService questionService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal AuthUser user, Model model) {
        model.addAttribute("quizzes", quizService.getQuizzesByFaculty(user.getId()));
        return "faculty/dashboard";
    }

    @PostMapping("/quizzes")
    public String createQuiz(@AuthenticationPrincipal AuthUser user,
                             @RequestParam String title,
                             @RequestParam(required = false) String description,
                             @RequestParam Integer timeLimit,
                             Model model) {
        try {
            QuizCreateRequest request = new QuizCreateRequest();
            request.setFacultyId(user.getId());
            request.setTitle(title);
            request.setDescription(description);
            request.setTimeLimit(timeLimit);

            quizService.createQuiz(request);
            return "redirect:/faculty/dashboard";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("quizzes", quizService.getQuizzesByFaculty(user.getId()));
            return "faculty/dashboard";
        }
    }
    @GetMapping("/quizzes/{quizId}/questions")
    public String questionsPage(@PathVariable Long quizId,
                                @AuthenticationPrincipal AuthUser user,
                                Model model) {

        QuizResponse quiz = quizService.getQuizById(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questionService.getQuestionsByQuiz(quizId, user.getId()));
        return "faculty/questions";
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public String addQuestion(@PathVariable Long quizId,
                              @AuthenticationPrincipal AuthUser user,
                              @RequestParam String questionText,
                              @RequestParam String optionA,
                              @RequestParam String optionB,
                              @RequestParam String optionC,
                              @RequestParam String optionD,
                              @RequestParam String correctOption,
                              @RequestParam Integer marks,
                              Model model) {
        try {
            QuestionCreateRequest request = new QuestionCreateRequest();
            request.setQuestionText(questionText);
            request.setOptionA(optionA);
            request.setOptionB(optionB);
            request.setOptionC(optionC);
            request.setOptionD(optionD);
            request.setCorrectOption(correctOption);
            request.setMarks(marks);

            questionService.addQuestion(quizId, user.getId(), request);
            return "redirect:/faculty/quizzes/" + quizId + "/questions";

        } catch (RuntimeException e) {
            model.addAttribute("quiz", quizService.getQuizById(quizId));
            model.addAttribute("questions", questionService.getQuestionsByQuiz(quizId, user.getId()));
            model.addAttribute("error", e.getMessage());
            return "faculty/questions";
        }
    }

    @PostMapping("/quizzes/{quizId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId,
                                 @PathVariable Long questionId,
                                 @AuthenticationPrincipal AuthUser user) {
        questionService.deleteQuestion(quizId, questionId, user.getId());
        return "redirect:/faculty/quizzes/" + quizId + "/questions";
    }
    @GetMapping("/quizzes/{quizId}/report")
    public String reportPage(@PathVariable Long quizId,
                             @RequestParam(required = false) String className,
                             @AuthenticationPrincipal AuthUser user,
                             Model model) {

        QuizResponse quiz = quizService.getQuizById(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("className", className);

        if (className != null && !className.isBlank()) {
            try {
                ClassReportResponse report = reportService.getClassReport(quizId, className, user.getId());
                model.addAttribute("report", report);
            } catch (RuntimeException e) {
                model.addAttribute("error", e.getMessage());
            }
        }

        return "faculty/report";
    }
}