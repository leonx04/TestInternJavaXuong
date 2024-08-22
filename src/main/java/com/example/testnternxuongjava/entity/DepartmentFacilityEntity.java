package com.example.testnternxuongjava.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * @author dungn
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "department_facility", schema = "dbo", catalog = "exam_distribution_test")
public class DepartmentFacilityEntity {
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

    @ManyToOne
    @JoinColumn(name = "id_department")
    private DepartmentEntity department;

    @ManyToOne
    @JoinColumn(name = "id_facility")
    private FacilityEntity facility;

    @ManyToOne
    @JoinColumn(name = "id_staff")
    private StaffEntity staff;
}
