package com.example.QuizWiz.repository;

import com.example.QuizWiz.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Student> findByClassName(String className);

    List<Student> findByClassNameIgnoreCase(String className);
}