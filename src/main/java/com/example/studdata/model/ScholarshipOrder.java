package com.example.studdata.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "scholarship_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"student"})
@EqualsAndHashCode(exclude = {"student"})
public class ScholarshipOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)  // ✅ ИЗМЕНЕНО НА EAGER
    @JoinColumn(name = "scholarship_id", nullable = false)
    @NotNull
    private Scholarship scholarship;

    @ManyToOne(fetch = FetchType.EAGER)  // ✅ ИЗМЕНЕНО НА EAGER
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
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "created_date")
    private LocalDate createdDate = LocalDate.now();

    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) {
            this.createdDate = LocalDate.now();
        }
        if (this.isPermanent == null) {
            this.isPermanent = false;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    public boolean isExpired() {
        if (Boolean.TRUE.equals(isPermanent)) {
            return false;
        }
        if (foundationEndDate == null) {
            return false;
        }
        return foundationEndDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringWithinDays(int days) {
        if (Boolean.TRUE.equals(isPermanent)) {
            return false;
        }
        if (foundationEndDate == null) {
            return false;
        }
        LocalDate expirationThreshold = LocalDate.now().plusDays(days);
        return foundationEndDate.isBefore(expirationThreshold) &&
                foundationEndDate.isAfter(LocalDate.now());
    }
}