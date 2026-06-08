package com.studyroom.config;

import com.studyroom.entity.Seat;
import com.studyroom.repository.SeatRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 预置测试数据
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final SeatRepository seatRepository;

    public DataInitializer(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public void run(String... args) {
        if (seatRepository.count() > 0) {
            return; // 已有数据不重复初始化
        }

        String[] areas = {"A区", "B区", "C区"};
        int seatsPerArea = 15;
        int cols = 5;

        for (String area : areas) {
            for (int i = 1; i <= seatsPerArea; i++) {
                String seatNum = area.substring(0, 1) + String.format("%02d", i);
                int row = (i - 1) / cols;
                int col = (i - 1) % cols;
                Seat seat = new Seat(seatNum, area, row, col);
                seatRepository.save(seat);
            }
        }

        System.out.println("测试数据初始化完成：共 " + seatRepository.count() + " 个座位");
    }
}