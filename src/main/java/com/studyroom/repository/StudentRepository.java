package com.studyroom.repository;

import com.studyroom.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNo(String studentNo);
    boolean existsByStudentNo(String studentNo);
}