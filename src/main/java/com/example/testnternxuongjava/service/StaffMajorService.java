// StaffMajorService.java
package com.example.testnternxuongjava.service;

import com.example.testnternxuongjava.entity.StaffEntity;
import com.example.testnternxuongjava.entity.StaffMajorFacilityEntity;
import com.example.testnternxuongjava.repository.StaffMajorFacilityRepo;
import com.example.testnternxuongjava.repository.StaffRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author dungn
 */
@Service
public class StaffMajorService {

    @Autowired
    private StaffMajorFacilityRepo staffMajorRepo;

    @Autowired
    private StaffRepo staffRepo;


    public StaffMajorFacilityEntity getStaffMajor(UUID id) {
        return staffMajorRepo.findByStaffId(id);
    }

    public void addStaffMajor(StaffMajorFacilityEntity staffMajor) {
        staffMajorRepo.save(staffMajor);
    }

    public void deleteStaffMajor(UUID id) {
        staffMajorRepo.deleteByStaffAndMajorFacility(staffRepo.findById(id).get(), staffMajorRepo.findByStaffId(id).getMajorFacility());
    }
}