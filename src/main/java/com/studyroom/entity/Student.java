package com.studyroom.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", nullable = false, unique = true, length = 20)
    private String studentNo;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    public Student() {}

    public Student(String studentNo, String name, String phone) {
        this.studentNo = studentNo;
        this.name = name;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}