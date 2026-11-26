
package com.smartparking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartparking.entity.ParkingRecord;

public interface ParkingRecordRepository extends JpaRepository<ParkingRecord, Long> {

    boolean existsByVehicleNumberAndExitTimeIsNull(String vehicleNumber);

    Optional<ParkingRecord> findByVehicleNumberAndExitTimeIsNull(
            String vehicleNumber);

    List<ParkingRecord> findByVehicleNumberOrderByEntryTimeDesc(
            String vehicleNumber);
}
