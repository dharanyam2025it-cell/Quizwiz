package com.example.QuizWiz.repository;

import com.example.QuizWiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByFacultyId(Long facultyId);
}