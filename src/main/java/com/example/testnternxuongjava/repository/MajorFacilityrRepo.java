package com.example.testnternxuongjava.repository;

import com.example.testnternxuongjava.entity.MajorFacilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author dungn
 */
@Repository
public interface MajorFacilityrRepo extends JpaRepository<MajorFacilityEntity, UUID> {
    Optional<MajorFacilityEntity> findByDepartmentFacilityDepartmentIdAndDepartmentFacilityFacilityIdAndMajorId(UUID departmentId, UUID facilityId, UUID majorId);

    Optional<MajorFacilityEntity> findByMajorNameAndDepartmentFacilityDepartmentNameAndDepartmentFacilityFacilityName(String majorName, String departmentName, String facilityName);
}
