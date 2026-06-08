package com.studyroom.service;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.Seat;
import com.studyroom.entity.Student;
import com.studyroom.enums.ReservationStatus;
import com.studyroom.enums.SeatStatus;
import com.studyroom.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatService seatService;
    private final StudentService studentService;

    public ReservationService(ReservationRepository reservationRepository,
                              SeatService seatService,
                              StudentService studentService) {
        this.reservationRepository = reservationRepository;
        this.seatService = seatService;
        this.studentService = studentService;
    }

    /**
     * 预约座位（含冲突检测）
     */
    public Reservation reserve(Long seatId, String studentNo, String studentName,
                               String studentPhone, LocalDateTime startTime, LocalDateTime endTime) {
        // 校验时间
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("开始时间必须早于结束时间");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("预约时间不能早于当前时间");
        }

        Seat seat = seatService.findById(seatId);
        if (seat.getStatus() == SeatStatus.DISABLED) {
            throw new IllegalArgumentException("该座位已停用");
        }

        // 冲突检测
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                seatId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("该座位在所选时间段内已被预约");
        }

        // 获取或创建学生
        Student student = studentService.findOrCreate(studentNo, studentName, studentPhone);

        Reservation reservation = new Reservation(seat, student, startTime, endTime);
        reservation = reservationRepository.save(reservation);

        // 更新座位状态
        seat.setStatus(SeatStatus.RESERVED);
        seatService.save(seat);

        return reservation;
    }

    /**
     * 签到入座
     */
    public Reservation checkIn(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalArgumentException("当前状态不可签到");
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setCheckinTime(LocalDateTime.now());

        // 更新座位状态为使用中
        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.IN_USE);
        seatService.save(seat);

        return reservationRepository.save(reservation);
    }

    /**
     * 离座
     */
    public Reservation checkOut(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new IllegalArgumentException("当前状态不可离座");
        }

        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.setCheckoutTime(LocalDateTime.now());

        // 释放座位
        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.FREE);
        seatService.save(seat);

        return reservationRepository.save(reservation);
    }

    /**
     * 取消预约
     */
    public Reservation cancel(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));

        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalArgumentException("当前状态不可取消");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        // 释放座位
        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.FREE);
        seatService.save(seat);

        return reservationRepository.save(reservation);
    }

    /**
     * 标记超时
     */
    public void markOverdue(Reservation reservation) {
        reservation.setStatus(ReservationStatus.TIMEOUT);

        Seat seat = reservation.getSeat();
        seat.setStatus(SeatStatus.FREE);
        seatService.save(seat);

        reservationRepository.save(reservation);
    }

    /**
     * 查询所有超时未签到的预约
     */
    public List<Reservation> getOverdueReservations() {
        return reservationRepository.findOverdueReservations(LocalDateTime.now());
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByTimeRange(start, end);
    }

    public List<Reservation> findByStudentId(Long studentId) {
        return reservationRepository.findByStudentId(studentId);
    }

    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约记录不存在"));
    }
}