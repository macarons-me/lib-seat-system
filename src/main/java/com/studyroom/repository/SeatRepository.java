package com.studyroom.repository;

import com.studyroom.entity.Seat;
import com.studyroom.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByArea(String area);
    List<Seat> findByStatus(SeatStatus status);
    long countByArea(String area);
    long countByAreaAndStatus(String area, SeatStatus status);
}