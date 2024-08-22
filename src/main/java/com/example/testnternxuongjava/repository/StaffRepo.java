package com.example.testnternxuongjava.repository;

import com.example.testnternxuongjava.entity.StaffEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.UUID;

/**
 * @author dungn
 */
public interface StaffRepo extends JpaRepository<StaffEntity, UUID> {

}
