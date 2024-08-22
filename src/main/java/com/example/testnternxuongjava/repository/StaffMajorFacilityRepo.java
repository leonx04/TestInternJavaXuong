package com.example.testnternxuongjava.repository;

import com.example.testnternxuongjava.entity.MajorFacilityEntity;
import com.example.testnternxuongjava.entity.StaffEntity;
import com.example.testnternxuongjava.entity.StaffMajorFacilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author dungn
 */
@Repository
public interface StaffMajorFacilityRepo extends JpaRepository<StaffMajorFacilityEntity, UUID> {
    StaffMajorFacilityEntity findByStaffId(UUID id);

    @Transactional
    @Modifying
    void deleteByStaffAndMajorFacility(StaffEntity staff, MajorFacilityEntity majorFacility);
}
