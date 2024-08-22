package com.example.testnternxuongjava.repository;

import com.example.testnternxuongjava.entity.MajorEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author dungn
 */
@Repository
public interface MajorRepo extends JpaRepository<MajorEntity, UUID> {
}
