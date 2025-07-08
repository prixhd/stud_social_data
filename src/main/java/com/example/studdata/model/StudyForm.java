package com.example.studdata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "study_forms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyForm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    public StudyForm(String name) {
        this.name = name;
    }
}