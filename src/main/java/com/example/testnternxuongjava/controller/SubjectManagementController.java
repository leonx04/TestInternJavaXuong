
package com.example.testnternxuongjava.controller;

import com.example.testnternxuongjava.entity.StaffMajorFacilityEntity;
import com.example.testnternxuongjava.service.StaffMajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author dungn
 */
@Controller
@RequestMapping("/staff/major")
public class SubjectManagementController {

    @Autowired
    private StaffMajorService staffMajorService;

    @GetMapping("/{id}")
    public String getStaffMajor(@PathVariable UUID id, Model model) {
        StaffMajorFacilityEntity staffMajor = staffMajorService.getStaffMajor(id);
        model.addAttribute("staffMajor", staffMajor);
        return "staff/staffmajor";
    }

    @PostMapping("/add")
    public String addStaffMajor(@ModelAttribute("staffMajor") StaffMajorFacilityEntity staffMajor) {
        staffMajorService.addStaffMajor(staffMajor);
        return "redirect:/staff/major/" + staffMajor.getStaff().getId();
    }

    @PostMapping("/delete/{id}")
    public String deleteStaffMajor(@PathVariable UUID id) {
        staffMajorService.deleteStaffMajor(id);
        return "redirect:/staff/major/" + id;
    }
}
