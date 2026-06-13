package com.example.studdata.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"faculty", "studyForm", "scholarship", "foundation", "scholarshipOrders"})
@EqualsAndHashCode(exclude = {"faculty", "studyForm", "scholarship", "foundation", "scholarshipOrders"})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank
    @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Size(max = 50)
    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Min(1)
    @Max(6)
    @Column(name = "course", nullable = false)
    private int course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    @NotNull
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_form_id", nullable = false)
    @NotNull
    private StudyForm studyForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scholarship_id", nullable = false)
    @NotNull
    private Scholarship scholarship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "foundation_id", nullable = false)
    @NotNull
    private Foundation foundation;

    @NotBlank
    @Size(max = 100)
    @Column(name = "order_number", nullable = false, length = 100)
    private String orderNumber;

    @NotNull
    @Column(name = "issuance_end_date", nullable = false)
    private LocalDate issuanceEndDate;

    @Column(name = "foundation_end_date")
    private LocalDate foundationEndDate;

    @Builder.Default
    @Column(name = "is_permanent")
    private Boolean isPermanent = false;

    @Builder.Default
    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Builder.Default
    @Column(name = "is_indefinite", nullable = false)
    private Boolean isIndefinite = false;

    // ✅ ДОБАВЛЕНО: Связь с историей приказов
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ScholarshipOrder> scholarshipOrders = new ArrayList<>();

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
        this.isIndefinite = false;
        this.scholarshipOrders = new ArrayList<>();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDate.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) {
            this.createdDate = LocalDate.now();
        }
        if (this.isPermanent == null) {
            this.isPermanent = false;
        }
        if (this.isIndefinite == null) {
            this.isIndefinite = false;
        }
    }

    // ✅ Вспомогательные методы для работы с приказами
    public void addOrder(ScholarshipOrder order) {
        scholarshipOrders.add(order);
        order.setStudent(this);
    }

    public void removeOrder(ScholarshipOrder order) {
        scholarshipOrders.remove(order);
        order.setStudent(null);
    }

    public Boolean getIsIndefinite() {
        return isIndefinite;
    }

    public void setIsIndefinite(Boolean isIndefinite) {
        this.isIndefinite = isIndefinite;
    }

    /**
     * Возвращает полное имя студента
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (lastName != null) sb.append(lastName);
        if (firstName != null) sb.append(" ").append(firstName);
        if (middleName != null && !middleName.isEmpty()) sb.append(" ").append(middleName);
        return sb.toString().trim();
    }
}