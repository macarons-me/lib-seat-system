package com.studyroom.service.statistics;

import com.studyroom.entity.Reservation;
import com.studyroom.entity.Seat;
import com.studyroom.enums.ReservationStatus;
import com.studyroom.repository.ReservationRepository;
import com.studyroom.repository.SeatRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自习室整体/各区域使用率统计
 */
@Component
public class UsageRateReport implements StatusReport {

    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;

    public UsageRateReport(SeatRepository seatRepository,
                           ReservationRepository reservationRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public String getReportType() {
        return "使用率统计";
    }

    @Override
    public Map<String, Object> generateReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportType", getReportType());
        report.put("generatedTime", LocalDateTime.now().toString());

        // 整体统计
        long totalSeats = seatRepository.count();
        long inUseSeats = seatRepository.findByStatus(com.studyroom.enums.SeatStatus.IN_USE).size();
        long reservedSeats = seatRepository.findByStatus(com.studyroom.enums.SeatStatus.RESERVED).size();
        long freeSeats = seatRepository.findByStatus(com.studyroom.enums.SeatStatus.FREE).size();
        long disabledSeats = seatRepository.findByStatus(com.studyroom.enums.SeatStatus.DISABLED).size();

        Map<String, Object> overall = new LinkedHashMap<>();
        overall.put("总座位数", totalSeats);
        overall.put("使用中", inUseSeats);
        overall.put("已预约", reservedSeats);
        overall.put("空闲", freeSeats);
        overall.put("停用", disabledSeats);
        double usageRate = totalSeats > 0
                ? (double) (inUseSeats + reservedSeats) / totalSeats * 100
                : 0;
        overall.put("使用率(%)", String.format("%.1f", usageRate));
        report.put("整体统计", overall);

        // 按区域统计
        List<Seat> allSeats = seatRepository.findAll();
        Map<String, List<Seat>> seatsByArea = allSeats.stream()
                .collect(Collectors.groupingBy(Seat::getArea));

        List<Map<String, Object>> areaStats = new ArrayList<>();
        for (Map.Entry<String, List<Seat>> entry : seatsByArea.entrySet()) {
            String area = entry.getKey();
            List<Seat> areaSeats = entry.getValue();
            long total = areaSeats.size();
            long inUse = areaSeats.stream().filter(s -> s.getStatus() == com.studyroom.enums.SeatStatus.IN_USE).count();
            long reserved = areaSeats.stream().filter(s -> s.getStatus() == com.studyroom.enums.SeatStatus.RESERVED).count();

            Map<String, Object> areaStat = new LinkedHashMap<>();
            areaStat.put("区域", area);
            areaStat.put("总座位数", total);
            areaStat.put("使用中", inUse);
            areaStat.put("已预约", reserved);
            double areaRate = total > 0 ? (double) (inUse + reserved) / total * 100 : 0;
            areaStat.put("使用率(%)", String.format("%.1f", areaRate));
            areaStats.add(areaStat);
        }
        report.put("各区域统计", areaStats);

        return report;
    }
}