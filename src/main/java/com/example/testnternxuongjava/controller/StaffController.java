package com.example.testnternxuongjava.controller;

import com.example.testnternxuongjava.entity.MajorFacilityEntity;
import com.example.testnternxuongjava.entity.StaffEntity;
import com.example.testnternxuongjava.entity.StaffMajorFacilityEntity;
import com.example.testnternxuongjava.repository.MajorFacilityrRepo;
import com.example.testnternxuongjava.repository.StaffMajorFacilityRepo;
import com.example.testnternxuongjava.repository.StaffRepo;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private StaffRepo staffRepo;
    @Autowired
    MajorFacilityrRepo majorFacilityRepo;
    @Autowired
    private StaffMajorFacilityRepo staffMajorFacilityRepo;
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

    @GetMapping("/download-template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Danh sách nhân viên");

    CellStyle headerStyle = workbook.createCellStyle();
    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setAlignment(HorizontalAlignment.LEFT);

    Row headerRow = sheet.createRow(0);
    String[] headers = {"STT", "Mã nhân viên", "Tên nhân viên", "Email FPT", "Email FE", "Bộ môn - Chuyên ngành - Cơ sở"};
    for (int i = 0; i < headers.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(headers[i]);
        cell.setCellStyle(headerStyle);
    }

    List<StaffEntity> staffList = staffRepo.findAll();

    int rowIndex = 1;
    for (StaffEntity staff : staffList) {
        Row row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(rowIndex - 1);
        row.createCell(1).setCellValue(staff.getStaffCode());
        row.createCell(2).setCellValue(staff.getName());
        row.createCell(3).setCellValue(staff.getAccountFpt());
        row.createCell(4).setCellValue(staff.getAccountFe());

        // Lấy bộ môn - chuyên ngành - cơ sở cho nhân viên này
        List<StaffMajorFacilityEntity> staffMajors = staffMajorFacilityRepo.findByStaffId(staff.getId());
        String majorInfo = staffMajors.stream()
                .map(sm -> sm.getMajorFacility().getMajor().getName() + " - " +
                        sm.getMajorFacility().getDepartmentFacility().getDepartment().getName() + " (" +
                        sm.getMajorFacility().getDepartmentFacility().getFacility().getName() + ")")
                .collect(Collectors.joining(", "));

        row.createCell(5).setCellValue(majorInfo);
    }

    // Ghi dữ liệu vào ByteArrayOutputStream
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    workbook.write(outputStream);
    workbook.close();

    // Trả về file dưới dạng ResponseEntity
    HttpHeaders headersResponse = new HttpHeaders();
    headersResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headersResponse.setContentDispositionFormData("attachment", "staff_template.xlsx");

    return ResponseEntity.ok()
            .headers(headersResponse)
            .body(outputStream.toByteArray());
    }

    @PostMapping("/import")
    public String importStaff(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Vui lòng chọn một file để tải lên");
            return "redirect:/staff/index";
        }

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            int importedCount = 0;

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Bỏ qua hàng tiêu đề
                if (currentRow.getRowNum() == 0) {
                    continue;
                }

                String staffCode = currentRow.getCell(1).getStringCellValue();
                String name = currentRow.getCell(2).getStringCellValue();
                String accountFpt = currentRow.getCell(3).getStringCellValue();
                String accountFe = currentRow.getCell(4).getStringCellValue();
                String majorInfo = currentRow.getCell(5).getStringCellValue();

                // Kiểm tra xem email FPT và email FE có chứa mã nhân viên không
                if (!accountFpt.contains(staffCode) || !accountFe.contains(staffCode)) {
                    continue;
                }

                StaffEntity staff = new StaffEntity();
                staff.setStaffCode(staffCode);
                staff.setName(name);
                staff.setAccountFpt(accountFpt);
                staff.setAccountFe(accountFe);

                // Lưu nhân viên vào cơ sở dữ liệu
                staffRepo.save(staff);
                importedCount++;

                // Tách thông tin "Bộ môn - Chuyên ngành - Cơ sở"
                String[] majorParts = majorInfo.split(" - ");
                if (majorParts.length < 3) {
                    // Nếu không đủ thông tin, bỏ qua hàng này
                    continue;
                }
                String majorName = majorParts[0];
                String departmentName = majorParts[1];
                String facilityName = majorParts[2].substring(1, majorParts[2].length() - 1);

                // Lấy bộ môn - chuyên ngành - cơ sở cho nhân viên không
                MajorFacilityEntity majorFacility = majorFacilityRepo.findByMajorNameAndDepartmentFacilityDepartmentNameAndDepartmentFacilityFacilityName(majorName, departmentName, facilityName)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ môn - chuyên ngành - cơ sở tương ứng"));

                // Tạo StaffMajorFacilityEntity mới
                StaffMajorFacilityEntity staffMajor = new StaffMajorFacilityEntity();
                staffRepo.save(staff);
                System.out.println("Saved staff: " + staff.getName());
                staffMajor.setMajorFacility(majorFacility);

                // Lưu StaffMajorFacilityEntity vào cơ sở dữ liệu
                staffMajorFacilityRepo.save(staffMajor);
            }

            workbook.close();
            redirectAttributes.addFlashAttribute("message", "File đã được tải lên thành công. Tổng số bản ghi đã import: " + importedCount);
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi khi tải lên file: " + e.getMessage());
        }

        return "redirect:/staff/index";
    }


}