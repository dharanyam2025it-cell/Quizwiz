package com.example.QuizWiz.controller;

import com.example.QuizWiz.dto.LoginRequest;
import com.example.QuizWiz.dto.StudentRegisterRequest;
import com.example.QuizWiz.dto.StudentResponse;
import com.example.QuizWiz.entity.Student;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.security.SessionAuthenticator;
import com.example.QuizWiz.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final SessionAuthenticator sessionAuthenticator;
    private final StudentService studentService;

    @PostMapping("/register")
    public ResponseEntity<StudentResponse> register(@RequestBody StudentRegisterRequest request) {

        Student saved = studentService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getClassName()
        );

        StudentResponse response = new StudentResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getClassName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<StudentResponse> login(@RequestBody LoginRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {

        Student student = studentService.login(request.getEmail(), request.getPassword());

        sessionAuthenticator.login(
                new AuthUser(student.getId(), student.getEmail(), student.getName(), "STUDENT"),
                httpRequest, httpResponse);

        return ResponseEntity.ok(new StudentResponse(
                student.getId(), student.getName(), student.getEmail(), student.getClassName()));
    }
}