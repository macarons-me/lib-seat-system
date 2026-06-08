package com.studyroom.service;

import com.studyroom.entity.Seat;
import com.studyroom.enums.SeatStatus;
import com.studyroom.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SeatService {

    private final SeatRepository seatRepository;

    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public Seat save(Seat seat) {
        return seatRepository.save(seat);
    }

    public Seat findById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("座位不存在: id=" + id));
    }

    public List<Seat> findAll() {
        return seatRepository.findAll();
    }

    public List<Seat> findByArea(String area) {
        return seatRepository.findByArea(area);
    }

    public Seat updateStatus(Long id, SeatStatus newStatus) {
        Seat seat = findById(id);
        seat.setStatus(newStatus);
        return seatRepository.save(seat);
    }
}