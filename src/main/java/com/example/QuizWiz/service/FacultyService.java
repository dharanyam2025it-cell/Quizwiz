package com.example.QuizWiz.service;

import com.example.QuizWiz.entity.Faculty;
import com.example.QuizWiz.exception.InvalidCredentialsException;
import com.example.QuizWiz.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;

    public Faculty register(String name, String email, String rawPassword) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (facultyRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        Faculty faculty = new Faculty();
        faculty.setName(name.trim());
        faculty.setEmail(normalizedEmail);
        faculty.setPassword(passwordEncoder.encode(rawPassword));

        return facultyRepository.save(faculty);
    }

    public Faculty login(String email, String rawPassword) {

        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        Faculty faculty = facultyRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, faculty.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return faculty;
    }
}