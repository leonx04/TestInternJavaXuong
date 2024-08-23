package com.example.testnternxuongjava.controller;

import com.example.testnternxuongjava.entity.*;
import com.example.testnternxuongjava.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author dungn
 */
@Controller
@RequestMapping("/staff-major")
public class SubjectManagementController {

    @Autowired
    private StaffRepo staffRepo;
    @Autowired
    private StaffMajorFacilityRepo staffMajorFacilityRepo;
    @Autowired
    private FacilityRepo facilityRepo;
    @Autowired
    private DepartmentFacilityRepo departmentFacilityRepo;
    @Autowired
    private MajorFacilityrRepo majorFacilityRepo;

    @GetMapping("/{staffId}")
    public String getStaffMajors(@PathVariable UUID staffId, Model model) {
        StaffEntity staff = staffRepo.findById(staffId).orElse(null);
        List<StaffMajorFacilityEntity> staffMajors = staffMajorFacilityRepo.findByStaffId(staffId);
        List<FacilityEntity> allFacilities = facilityRepo.findAll();

        // Lọc ra các cơ sở mà nhân viên chưa được gán bộ môn chuyên ngành
        List<FacilityEntity> availableFacilities = allFacilities.stream()
                .filter(facility -> staffMajors.stream()
                        .noneMatch(sm -> sm.getMajorFacility().getDepartmentFacility().getFacility().getId().equals(facility.getId())))
                .collect(Collectors.toList());

        model.addAttribute("staff", staff);
        model.addAttribute("staffMajors", staffMajors);
        model.addAttribute("availableFacilities", availableFacilities);

        return "staff/staffmajor";
    }

    @PostMapping("/{staffId}/add")
    public String addStaffMajor(@PathVariable UUID staffId,
                                @RequestParam UUID facilityId,
                                @RequestParam UUID departmentId,
                                @RequestParam UUID majorId,
                                RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra xem nhân viên đã có bộ môn/chuyên ngành cho cơ sở này chưa
            boolean exists = staffMajorFacilityRepo.existsByStaffIdAndMajorFacilityDepartmentFacilityFacilityId(staffId, facilityId);
            if (exists) {
                throw new RuntimeException("Nhân viên đã có bộ môn chuyên ngành trong cơ sở này");
            }

            StaffEntity staff = staffRepo.findById(staffId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

            MajorFacilityEntity majorFacility = majorFacilityRepo.findByDepartmentFacilityDepartmentIdAndDepartmentFacilityFacilityIdAndMajorId(departmentId, facilityId, majorId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy cơ sở chính xác cho chuyên ngành này"));

            StaffMajorFacilityEntity newStaffMajor = new StaffMajorFacilityEntity();
            newStaffMajor.setStaff(staff);
            newStaffMajor.setMajorFacility(majorFacility);

            staffMajorFacilityRepo.save(newStaffMajor);

            redirectAttributes.addFlashAttribute("success", "Bộ môn và chuyên ngành đã được thêm thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff-major/" + staffId;
    }

    @GetMapping("/get-all-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllData(@RequestParam UUID staffId) {
        Map<String, Object> data = new HashMap<>();

        List<FacilityEntity> facilities = facilityRepo.findAll();
        List<StaffMajorFacilityEntity> staffMajors = staffMajorFacilityRepo.findByStaffId(staffId);

        // Lọc ra các cơ sở mà nhân viên chưa được gán bộ môn chuyên ngành
        List<FacilityEntity> availableFacilities = facilities.stream()
                .filter(facility -> staffMajors.stream()
                        .noneMatch(sm -> sm.getMajorFacility()
                        .getDepartmentFacility()
                        .getFacility()
                        .getId()
                        .equals(facility.getId())))
                .collect(Collectors.toList());

        data.put("facilities", availableFacilities);

        List<DepartmentFacilityEntity> departmentFacilities = departmentFacilityRepo.findAll();
        data.put("departmentFacilities", departmentFacilities);

        List<MajorFacilityEntity> majorFacilities = majorFacilityRepo.findAll();
        data.put("majorFacilities", majorFacilities);

        return ResponseEntity.ok(data);
    }

    @PostMapping("/delete/{staffId}")
    public String deleteStaffMajor(@PathVariable UUID staffId,
                                   @RequestParam UUID facilityId,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Tìm StaffMajorFacilityEntity dựa trên staffId và facilityId
            List<StaffMajorFacilityEntity> staffMajors = staffMajorFacilityRepo.
                    findByStaffIdAndMajorFacilityDepartmentFacilityFacilityId(staffId, facilityId);

            if (staffMajors.isEmpty()) {
                throw new RuntimeException("Không tìm thấy bản ghi cho nhân viên và cơ sở này");
            }

            // Xóa  bản ghi tìm thấy
            staffMajorFacilityRepo.deleteAll(staffMajors);

            redirectAttributes.addFlashAttribute("success", "Bộ môn và chuyên ngành đã được xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff-major/" + staffId;
    }
}
