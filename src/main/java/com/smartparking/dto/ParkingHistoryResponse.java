
package com.smartparking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ParkingHistoryResponse {

    private String vehicleNumber;
    private String vehicleType;
    private String slotNumber;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long durationMinutes;
    private BigDecimal parkingFee;

    public ParkingHistoryResponse(String vehicleNumber, String vehicleType,
            String slotNumber, LocalDateTime entryTime,
            LocalDateTime exitTime, Long durationMinutes,
            BigDecimal parkingFee) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.slotNumber = slotNumber;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.durationMinutes = durationMinutes;
        this.parkingFee = parkingFee;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    public String getSlotNumber() { return slotNumber; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public Long getDurationMinutes() { return durationMinutes; }
    public BigDecimal getParkingFee() { return parkingFee; }
}
