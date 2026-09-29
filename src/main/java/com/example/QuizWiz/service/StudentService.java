package com.example.QuizWiz.service;

import com.example.QuizWiz.entity.Student;
import com.example.QuizWiz.exception.InvalidCredentialsException;
import com.example.QuizWiz.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public Student register(String name, String email,
                            String rawPassword, String className) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Class name is required");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (studentRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        Student student = new Student();
        student.setName(name.trim());
        student.setEmail(normalizedEmail);
        student.setPassword(passwordEncoder.encode(rawPassword));
        student.setClassName(className.trim());

        return studentRepository.save(student);
    }

    public Student login(String email, String rawPassword) {

        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        Student student = studentRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, student.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return student;
    }
}