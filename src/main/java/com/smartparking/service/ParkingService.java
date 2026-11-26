
package com.smartparking.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartparking.dto.ParkingHistoryResponse;
import com.smartparking.dto.ParkingRequest;
import com.smartparking.dto.ParkingResponse;
import com.smartparking.dto.ParkingSlotRequest;
import com.smartparking.entity.ParkingRecord;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.enums.SlotStatus;
import com.smartparking.enums.VehicleType;
import com.smartparking.exception.BadRequestException;
import com.smartparking.exception.ResourceNotFoundException;
import com.smartparking.repository.ParkingRecordRepository;
import com.smartparking.repository.ParkingSlotRepository;

@Service
public class ParkingService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingRecordRepository recordRepository;

    public ParkingService(ParkingSlotRepository slotRepository,
                          ParkingRecordRepository recordRepository) {
        this.slotRepository = slotRepository;
        this.recordRepository = recordRepository;
    }

    // Register a parking slot
    @Transactional
    public ParkingSlot registerSlot(ParkingSlotRequest request) {

        String slotNumber = request.getSlotNumber()
                .trim().toUpperCase();

        if (slotRepository.existsBySlotNumber(slotNumber)) {
            throw new BadRequestException("Slot number already exists");
        }

        ParkingSlot slot = new ParkingSlot();
        slot.setSlotNumber(slotNumber);
        slot.setVehicleType(request.getVehicleType());
        slot.setStatus(SlotStatus.AVAILABLE);

        return slotRepository.save(slot);
    }

    // Allocate a parking slot
    @Transactional
    public ParkingResponse allocateSlot(ParkingRequest request) {

        String vehicleNumber = request.getVehicleNumber()
                .trim().toUpperCase();

        if (recordRepository.existsByVehicleNumberAndExitTimeIsNull(
                vehicleNumber)) {
            throw new BadRequestException("Vehicle is already parked");
        }

        List<ParkingSlot> slots =
                slotRepository.findAvailableSlotsForUpdate(
                        request.getVehicleType(), SlotStatus.AVAILABLE);

        if (slots.isEmpty()) {
            throw new BadRequestException(
                    "No available slot for this vehicle type");
        }

        ParkingSlot slot = slots.get(0);
        slot.setStatus(SlotStatus.OCCUPIED);

        ParkingRecord record = new ParkingRecord();
        record.setVehicleNumber(vehicleNumber);
        record.setVehicleType(request.getVehicleType());
        record.setParkingSlot(slot);
        record.setEntryTime(LocalDateTime.now());

        slotRepository.save(slot);
        recordRepository.save(record);

        return new ParkingResponse(
                "Parking slot allocated successfully",
                vehicleNumber,
                slot.getSlotNumber(),
                record.getEntryTime(),
                null,
                null,
                null);
    }

    // Release parking slot and calculate fee
    @Transactional
    public ParkingResponse releaseSlot(String vehicleNumber) {

        final String normalizedVehicleNumber =
                vehicleNumber.trim().toUpperCase();

        ParkingRecord record = recordRepository
                .findByVehicleNumberAndExitTimeIsNull(
                        normalizedVehicleNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active parking record found for vehicle: "
                                + normalizedVehicleNumber));

        LocalDateTime exitTime = LocalDateTime.now();

        long durationMinutes = Duration.between(
                record.getEntryTime(), exitTime).toMinutes();

        long billedHours = Math.max(
                1, (durationMinutes + 59) / 60);

        BigDecimal fee = calculateFee(
                record.getVehicleType(), billedHours);

        record.setExitTime(exitTime);
        record.setDurationMinutes(durationMinutes);
        record.setParkingFee(fee);

        ParkingSlot slot = record.getParkingSlot();
        slot.setStatus(SlotStatus.AVAILABLE);

        recordRepository.save(record);
        slotRepository.save(slot);

        return new ParkingResponse(
                "Parking slot released successfully",
                record.getVehicleNumber(),
                slot.getSlotNumber(),
                record.getEntryTime(),
                exitTime,
                durationMinutes,
                fee);
    }

    // Calculate parking fee
    private BigDecimal calculateFee(VehicleType type, long billedHours) {

        long firstHour;
        long additionalHour;

        if (type == VehicleType.TWO_WHEELER) {
            firstHour = 20;
            additionalHour = 10;
        } else {
            firstHour = 50;
            additionalHour = 30;
        }

        long totalFee = firstHour
                + (billedHours - 1) * additionalHour;

        return BigDecimal.valueOf(totalFee);
    }

    // View available slots
    @Transactional(readOnly = true)
    public List<ParkingSlot> getAvailableSlots(VehicleType type) {

        return slotRepository.findByVehicleTypeAndStatus(
                type, SlotStatus.AVAILABLE);
    }

    // View parking history
    @Transactional(readOnly = true)
    public List<ParkingHistoryResponse> getParkingHistory(
            String vehicleNumber) {

        String normalizedVehicleNumber =
                vehicleNumber.trim().toUpperCase();

        List<ParkingRecord> records =
                recordRepository.findByVehicleNumberOrderByEntryTimeDesc(
                        normalizedVehicleNumber);

        if (records.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No parking history found for vehicle: "
                            + normalizedVehicleNumber);
        }

        return records.stream()
                .map(record -> new ParkingHistoryResponse(
                        record.getVehicleNumber(),
                        record.getVehicleType().name(),
                        record.getParkingSlot().getSlotNumber(),
                        record.getEntryTime(),
                        record.getExitTime(),
                        record.getDurationMinutes(),
                        record.getParkingFee()))
                .toList();
    }
}
