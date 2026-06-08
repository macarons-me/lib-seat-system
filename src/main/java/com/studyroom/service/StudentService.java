package com.studyroom.service;

import com.studyroom.entity.Student;
import com.studyroom.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public Student findByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo)
                .orElseThrow(() -> new RuntimeException("学生不存在: 学号=" + studentNo));
    }

    public Student findOrCreate(String studentNo, String name, String phone) {
        return studentRepository.findByStudentNo(studentNo)
                .orElseGet(() -> studentRepository.save(new Student(studentNo, name, phone)));
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }
}