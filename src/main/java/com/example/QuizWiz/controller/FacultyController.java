package com.example.QuizWiz.controller;

import com.example.QuizWiz.dto.FacultyRegisterRequest;
import com.example.QuizWiz.dto.FacultyResponse;
import com.example.QuizWiz.dto.LoginRequest;
import com.example.QuizWiz.entity.Faculty;
import com.example.QuizWiz.security.AuthUser;
import com.example.QuizWiz.security.SessionAuthenticator;
import com.example.QuizWiz.service.FacultyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {
    private final SessionAuthenticator sessionAuthenticator;
    private final FacultyService facultyService;

    @PostMapping("/register")
    public ResponseEntity<FacultyResponse> register(@RequestBody FacultyRegisterRequest request) {

        Faculty saved = facultyService.register(
                request.getName(),
                request.getEmail(),
                request.getPassword()
        );

        FacultyResponse response = new FacultyResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<FacultyResponse> login(@RequestBody LoginRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {

        Faculty faculty = facultyService.login(request.getEmail(), request.getPassword());

        sessionAuthenticator.login(
                new AuthUser(faculty.getId(), faculty.getEmail(), faculty.getName(), "FACULTY"),
                httpRequest, httpResponse);

        return ResponseEntity.ok(new FacultyResponse(
                faculty.getId(), faculty.getName(), faculty.getEmail()));
    }

}