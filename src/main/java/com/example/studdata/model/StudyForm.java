package com.example.studdata.model;

import jakarta.persistence.*;

@Entity
@Table(name = "study_forms")
public class StudyForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    public StudyForm() {}

    public StudyForm(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}