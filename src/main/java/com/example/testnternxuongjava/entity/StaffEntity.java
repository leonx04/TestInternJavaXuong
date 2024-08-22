package com.example.testnternxuongjava.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "staff", schema = "dbo", catalog = "exam_distribution_test")
public class StaffEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "status")
    private Integer status;

    @Column(name = "created_date")
    private Long createdDate;

    @Column(name = "last_modified_date")
    private Long lastModifiedDate;

    @NotBlank(message = "Email FE không được để trống")
    @Size(max = 100, message = "Email FE không được quá 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@fe\\.edu\\.vn$", message = "Email FE phải kết thúc bằng @fe.edu.vn và không chứa khoảng trắng hoặc ký tự đặc biệt")
    @Column(name = "account_fe", unique = true)
    private String accountFe;

    @NotBlank(message = "Email FPT không được để trống")
    @Size(max = 100, message = "Email FPT không được quá 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@fpt\\.edu\\.vn$", message = "Email FPT phải kết thúc bằng @fpt.edu.vn và không chứa khoảng trắng hoặc ký tự đặc biệt")
    @Column(name = "account_fpt", unique = true)
    private String accountFpt;

    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được quá 100 ký tự")
    @Column(name = "name")
    private String name;

    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 15, message = "Mã nhân viên không được quá 15 ký tự")
    @Column(name = "staff_code", unique = true)
    private String staffCode;
}