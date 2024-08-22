package com.example.testnternxuongjava.repository;

import com.example.testnternxuongjava.entity.FacilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author dungn
 */
@Repository
public interface FacilityRepo extends JpaRepository<FacilityEntity, UUID> {
}
