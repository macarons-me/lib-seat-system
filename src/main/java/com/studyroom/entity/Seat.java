package com.studyroom.entity;

import com.studyroom.enums.SeatStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", nullable = false, unique = true)
    private String seatNumber;

    @Column(name = "area", nullable = false)
    private String area;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status = SeatStatus.FREE;

    @Column(name = "row_index")
    private Integer rowIndex;

    @Column(name = "col_index")
    private Integer colIndex;

    public Seat() {}

    public Seat(String seatNumber, String area, Integer rowIndex, Integer colIndex) {
        this.seatNumber = seatNumber;
        this.area = area;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.status = SeatStatus.FREE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }
    public Integer getColIndex() { return colIndex; }
    public void setColIndex(Integer colIndex) { this.colIndex = colIndex; }
}