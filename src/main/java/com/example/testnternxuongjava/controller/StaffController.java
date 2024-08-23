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
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn một file để tải lên");
            return "redirect:/staff/index";
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int importedCount = 0;
            int rowIndex = 0;
            StringBuilder errorLog = new StringBuilder();

            for (Row row : sheet) {
                rowIndex++;
                if (rowIndex == 1) {
                    continue;
                }

                try {
                    String staffCode = getCellValueAsString(row.getCell(1));
                    String name = getCellValueAsString(row.getCell(2));
                    String accountFpt = getCellValueAsString(row.getCell(3));
                    String accountFe = getCellValueAsString(row.getCell(4));
                    String majorInfo = getCellValueAsString(row.getCell(5));

                    System.out.println("Processing row " + rowIndex + ":");
                    System.out.println("Staff Code: " + staffCode);
                    System.out.println("Name: " + name);
                    System.out.println("FPT Account: " + accountFpt);
                    System.out.println("FE Account: " + accountFe);
                    System.out.println("Major Info: " + majorInfo);

                    if (!isValidEmail(accountFpt, staffCode) || !isValidEmail(accountFe, staffCode)) {
                        throw new RuntimeException("Email FPT hoặc FE không hợp lệ hoặc không chứa mã nhân viên");
                    }

                    StaffEntity staff = new StaffEntity();
                    staff.setStaffCode(staffCode);
                    staff.setName(name);
                    staff.setAccountFpt(accountFpt);
                    staff.setAccountFe(accountFe);
                    staff.setStatus(1);
                    staff.setCreatedDate(System.currentTimeMillis());

                    staffRepo.save(staff);
                    System.out.println("Staff saved successfully");

                    // Process major info
                    String[] majorParts = majorInfo.split(" - ");
                    if (majorParts.length < 3) {
                        throw new RuntimeException("Thông tin bộ môn - chuyên ngành - cơ sở không hợp lệ");
                    }

                    String majorName = majorParts[0].trim();
                    String departmentName = majorParts[1].trim();
                    String facilityName = majorParts[2].replaceAll("[()]", "").trim();

                    System.out.println("Major: " + majorName);
                    System.out.println("Department: " + departmentName);
                    System.out.println("Facility: " + facilityName);


                    MajorFacilityEntity majorFacility = majorFacilityRepo
                            .findByMajorNameAndDepartmentFacilityDepartmentNameAndDepartmentFacilityFacilityName(
                                    majorName, departmentName, facilityName)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy bộ môn - chuyên ngành - cơ sở tương ứng"));

                    System.out.println("MajorFacility found");


                    StaffMajorFacilityEntity staffMajor = new StaffMajorFacilityEntity();
                    staffMajor.setStaff(staff);
                    staffMajor.setMajorFacility(majorFacility);
                    staffMajor.setStatus(0);
                    staffMajor.setCreatedDate(System.currentTimeMillis());

                    staffMajorFacilityRepo.save(staffMajor);
                    System.out.println("StaffMajorFacility saved successfully");

                    importedCount++;
                } catch (Exception e) {
                    String errorMessage = "Error processing row " + rowIndex + ": " + e.getMessage();
                    System.err.println(errorMessage);
                    errorLog.append(errorMessage).append("\n");
                }
            }

            if (importedCount > 0) {
                redirectAttributes.addFlashAttribute("success", "Import thành công. Số bản ghi đã import: " + importedCount);
            } else {
                redirectAttributes.addFlashAttribute("error", "Không có bản ghi nào được import thành công.");
            }

            if (errorLog.length() > 0) {
                redirectAttributes.addFlashAttribute("errorLog", errorLog.toString());
            }
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đọc file: " + e.getMessage());
        }

        return "redirect:/staff/index";
    }

    private boolean isValidEmail(String email, String staffCode) {
        if (email == null || email.isEmpty() || staffCode == null || staffCode.isEmpty()) {
            return false;
        }

        // Remove any potential whitespace
        email = email.trim().toLowerCase();
        staffCode = staffCode.trim().toLowerCase();

        // Check if email ends with either @fpt.edu.vn or @fe.edu.vn
        if (!email.endsWith("@fpt.edu.vn") && !email.endsWith("@fe.edu.vn")) {
            return false;
        }

        // Extract the part before @ and check if it contains the staff code
        String localPart = email.split("@")[0];

        // Check if the local part contains the staff code
        return localPart.contains(staffCode);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}