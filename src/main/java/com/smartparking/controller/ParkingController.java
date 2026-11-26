
package com.smartparking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartparking.dto.ParkingHistoryResponse;
import com.smartparking.dto.ParkingRequest;
import com.smartparking.dto.ParkingResponse;
import com.smartparking.dto.ParkingSlotRequest;
import com.smartparking.entity.ParkingSlot;
import com.smartparking.enums.VehicleType;
import com.smartparking.service.ParkingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    // Register parking slot
    @PostMapping("/parking-slots")
    public ResponseEntity<ParkingSlot> registerSlot(
            @Valid @RequestBody ParkingSlotRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(parkingService.registerSlot(request));
    }

    // Allocate parking slot
    @PostMapping("/parking/allocate")
    public ResponseEntity<ParkingResponse> allocateSlot(
            @Valid @RequestBody ParkingRequest request) {

        return ResponseEntity.ok(
                parkingService.allocateSlot(request));
    }

    // Release parking slot
    @PostMapping("/parking/release/{vehicleNumber}")
    public ResponseEntity<ParkingResponse> releaseSlot(
            @PathVariable String vehicleNumber) {

        return ResponseEntity.ok(
                parkingService.releaseSlot(vehicleNumber));
    }

    // Get available slots by vehicle type
    @GetMapping("/parking-slots/available")
    public ResponseEntity<List<ParkingSlot>> getAvailableSlots(
            @RequestParam VehicleType type) {

        return ResponseEntity.ok(
                parkingService.getAvailableSlots(type));
    }

    // Get parking history
    @GetMapping("/parking/history/{vehicleNumber}")
    public ResponseEntity<List<ParkingHistoryResponse>> getParkingHistory(
            @PathVariable String vehicleNumber) {

        return ResponseEntity.ok(
                parkingService.getParkingHistory(vehicleNumber));
    }
}
