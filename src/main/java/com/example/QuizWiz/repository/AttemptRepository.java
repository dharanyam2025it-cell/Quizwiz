package com.example.QuizWiz.repository;

import com.example.QuizWiz.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    List<Attempt> findByStudentId(Long studentId);

    List<Attempt> findByQuizId(Long quizId);

    boolean existsByStudentIdAndQuizId(Long studentId, Long quizId);

    boolean existsByQuizId(Long quizId);
}