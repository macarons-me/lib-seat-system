package com.studyroom.repository;

import com.studyroom.entity.Reservation;
import com.studyroom.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySeatIdAndStatusNot(Long seatId, ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.seat.id = :seatId " +
           "AND r.status NOT IN ('CANCELLED', 'COMPLETED', 'TIMEOUT') " +
           "AND r.startTime < :endTime AND r.endTime > :startTime")
    List<Reservation> findConflictingReservations(
            @Param("seatId") Long seatId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT r FROM Reservation r WHERE r.status = 'RESERVED' " +
           "AND r.startTime < :now AND r.checkinTime IS NULL")
    List<Reservation> findOverdueReservations(@Param("now") LocalDateTime now);

    List<Reservation> findByStudentId(Long studentId);

    List<Reservation> findByStatus(ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.startTime >= :start AND r.endTime <= :end")
    List<Reservation> findByTimeRange(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    long countBySeatIdAndStatusNot(Long seatId, ReservationStatus status);
}