package com.example.testnternxuongjava.repository;

import com.example.testnternxuongjava.entity.DepartmentEntity;
import com.example.testnternxuongjava.entity.DepartmentFacilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * @author dungn
 */
@Repository
public interface DepartmentRepo  extends JpaRepository<DepartmentEntity, UUID> {

}
