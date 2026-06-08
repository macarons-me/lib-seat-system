package com.studyroom.entity;

import com.studyroom.enums.ReservationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status = ReservationStatus.RESERVED;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime;

    @Column(name = "checkout_time")
    private LocalDateTime checkoutTime;

    public Reservation() {}

    public Reservation(Seat seat, Student student, LocalDateTime startTime, LocalDateTime endTime) {
        this.seat = seat;
        this.student = student;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = ReservationStatus.RESERVED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Seat getSeat() { return seat; }
    public void setSeat(Seat seat) { this.seat = seat; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public LocalDateTime getCheckinTime() { return checkinTime; }
    public void setCheckinTime(LocalDateTime checkinTime) { this.checkinTime = checkinTime; }
    public LocalDateTime getCheckoutTime() { return checkoutTime; }
    public void setCheckoutTime(LocalDateTime checkoutTime) { this.checkoutTime = checkoutTime; }
}