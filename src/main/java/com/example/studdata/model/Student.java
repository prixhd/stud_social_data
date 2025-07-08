package com.example.studdata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "course", nullable = false)
    private int course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_form_id", nullable = false)
    private StudyForm studyForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id", nullable = false)
    private Scholarship scholarship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foundation_id", nullable = false)
    private Foundation foundation;

    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    @Column(name = "issuance_end_date", nullable = false)
    private LocalDate issuanceEndDate;

    @Column(name = "foundation_end_date")
    private LocalDate foundationEndDate;

    @Column(name = "is_permanent")
    @Builder.Default
    private Boolean isPermanent = false;

    @Column(name = "created_date")
    @Builder.Default
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public Student(String firstName, String lastName, String middleName,
                   int course, Faculty faculty, StudyForm studyForm,
                   Scholarship scholarship, Foundation foundation,
                   String orderNumber, LocalDate issuanceEndDate,
                   LocalDate foundationEndDate, Boolean isPermanent) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.course = course;
        this.faculty = faculty;
        this.studyForm = studyForm;
        this.scholarship = scholarship;
        this.foundation = foundation;
        this.orderNumber = orderNumber;
        this.issuanceEndDate = issuanceEndDate;
        this.foundationEndDate = foundationEndDate;
        this.isPermanent = isPermanent != null ? isPermanent : false;
        this.createdDate = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDate.now();
    }

    @Column(name = "is_indefinite", nullable = false)
    @Builder.Default
    private Boolean isIndefinite = false;

    public Boolean getIsIndefinite() {
        return isIndefinite;
    }

    public void setIsIndefinite(Boolean isIndefinite) {
        this.isIndefinite = isIndefinite;
    }
}