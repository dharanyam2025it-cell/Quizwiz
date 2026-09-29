package com.example.QuizWiz.web;

import com.example.QuizWiz.entity.Faculty;
import com.example.QuizWiz.entity.Student;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.security.SessionAuthenticator;
import com.example.QuizWiz.service.FacultyService;
import com.example.QuizWiz.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final FacultyService facultyService;
    private final StudentService studentService;
    private final SessionAuthenticator sessionAuthenticator;

    // ---------- LOGIN PAGE ----------
    @GetMapping("/login")
    public String loginPage(@RequestParam(defaultValue = "student") String role, Model model) {
        model.addAttribute("role", role);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String role,
                          @RequestParam String email,
                          @RequestParam String password,
                          HttpServletRequest request,
                          HttpServletResponse response,
                          Model model) {
        try {
            if ("faculty".equals(role)) {
                Faculty faculty = facultyService.login(email, password);
                sessionAuthenticator.login(
                        new AuthUser(faculty.getId(), faculty.getEmail(), faculty.getName(), "FACULTY"),
                        request, response);
                return "redirect:/faculty/dashboard";
            } else {
                Student student = studentService.login(email, password);
                sessionAuthenticator.login(
                        new AuthUser(student.getId(), student.getEmail(), student.getName(), "STUDENT"),
                        request, response);
                return "redirect:/student/dashboard";
            }
        } catch (RuntimeException e) {
            model.addAttribute("role", role);
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    // ---------- REGISTER PAGE ----------
    @GetMapping("/register")
    public String registerPage(@RequestParam(defaultValue = "student") String role, Model model) {
        model.addAttribute("role", role);
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String role,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam(required = false) String className,
                             Model model) {
        try {
            if ("faculty".equals(role)) {
                facultyService.register(name, email, password);
            } else {
                studentService.register(name, email, password, className);
            }
            return "redirect:/login?role=" + role;
        } catch (RuntimeException e) {
            model.addAttribute("role", role);
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    // ---------- HOME ----------
    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }
}