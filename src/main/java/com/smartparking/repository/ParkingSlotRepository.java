
package com.smartparking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.smartparking.entity.ParkingSlot;
import com.smartparking.enums.SlotStatus;
import com.smartparking.enums.VehicleType;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {

    boolean existsBySlotNumber(String slotNumber);

    List<ParkingSlot> findByVehicleTypeAndStatus(
            VehicleType vehicleType, SlotStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT p FROM ParkingSlot p
           WHERE p.vehicleType = :type
           AND p.status = :status
           ORDER BY p.id
           """)
    List<ParkingSlot> findAvailableSlotsForUpdate(
            @Param("type") VehicleType type,
            @Param("status") SlotStatus status);
}
