package com.example.testnternxuongjava.controller;

import com.example.testnternxuongjava.entity.StaffEntity;
import com.example.testnternxuongjava.repository.StaffRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffRepo staffRepo;

    @GetMapping("/index")
    public String getStaff(@RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "5") int size,
                           Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StaffEntity> staffPage = staffRepo.findAll(pageable);

        model.addAttribute("staffPage", staffPage);
        if (!model.containsAttribute("staffForm")) {
            model.addAttribute("staffForm", new StaffEntity());
        }
        return "staff/index";
    }

    @PostMapping("/create")
    public String createStaff(@Valid @ModelAttribute("staffForm") StaffEntity staff,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            Pageable pageable = PageRequest.of(0, 5);
            Page<StaffEntity> staffPage = staffRepo.findAll(pageable);
            model.addAttribute("staffPage", staffPage);
            model.addAttribute("showModal", true);
            return "staff/index";
        }
        staff.setStatus(0);
        staff.setCreatedDate(System.currentTimeMillis());
        staffRepo.save(staff);
        redirectAttributes.addFlashAttribute("successMessage", "Nhân viên đã được thêm thành công");
        return "redirect:/staff/index";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        StaffEntity staff = staffRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        staff.setStatus(staff.getStatus() == 0 ? 1 : 0);
        staffRepo.save(staff);
        redirectAttributes.addFlashAttribute("successMessage", "Trạng thái nhân viên đã được thay đổi");
        return "redirect:/staff/index";
    }
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        StaffEntity staff = staffRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        model.addAttribute("staff", staff);
        return "staff/update";
    }

    @PostMapping("/update")
    public String updateStaff(@Valid @ModelAttribute("staff") StaffEntity staff,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            // Giữ lại thông tin nhân viên nếu có lỗi và trả về form cập nhật với thông báo lỗi
            model.addAttribute("staff", staff);
            return "staff/update";
        }

        // Tìm kiếm nhân viên hiện có trong cơ sở dữ liệu
        StaffEntity existingStaff = staffRepo.findById(staff.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // Cập nhật các thuộc tính của nhân viên
        existingStaff.setStaffCode(staff.getStaffCode());
        existingStaff.setName(staff.getName());
        existingStaff.setAccountFpt(staff.getAccountFpt());
        existingStaff.setAccountFe(staff.getAccountFe());
        existingStaff.setStatus(staff.getStatus());
        existingStaff.setLastModifiedDate(System.currentTimeMillis());

        // Lưu nhân viên đã cập nhật vào cơ sở dữ liệu
        staffRepo.save(existingStaff);

        // Thêm thông báo thành công và chuyển hướng về trang danh sách nhân viên
        redirectAttributes.addFlashAttribute("successMessage", "Nhân viên đã được cập nhật thành công");
        return "redirect:/staff/index";
    }

}