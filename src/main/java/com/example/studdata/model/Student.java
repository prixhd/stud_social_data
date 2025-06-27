package com.example.studdata.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String middleName;
    private int course;

    @ManyToOne
    @JoinColumn(name = "faculty_id")
    private Faculty faculty;

    @ManyToOne
    @JoinColumn(name = "study_form_id")
    private StudyForm studyForm;

    @ManyToOne
    @JoinColumn(name = "scholarship_id")
    private Scholarship scholarship;

    @ManyToOne
    @JoinColumn(name = "foundation_id")
    private Foundation foundation;

    private String orderNumber;
    private LocalDate issuanceEndDate;
    private LocalDate foundationEndDate;

    @Column(name = "is_permanent")
    private Boolean isPermanent = false;

    public Student() {}

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
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public int getCourse() { return course; }
    public void setCourse(int course) { this.course = course; }

    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }

    public StudyForm getStudyForm() { return studyForm; }
    public void setStudyForm(StudyForm studyForm) { this.studyForm = studyForm; }

    public Scholarship getScholarship() { return scholarship; }
    public void setScholarship(Scholarship scholarship) { this.scholarship = scholarship; }

    public Foundation getFoundation() { return foundation; }
    public void setFoundation(Foundation foundation) { this.foundation = foundation; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public LocalDate getIssuanceEndDate() { return issuanceEndDate; }
    public void setIssuanceEndDate(LocalDate issuanceEndDate) { this.issuanceEndDate = issuanceEndDate; }

    public LocalDate getFoundationEndDate() { return foundationEndDate; }
    public void setFoundationEndDate(LocalDate foundationEndDate) { this.foundationEndDate = foundationEndDate; }

    public Boolean getIsPermanent() { return isPermanent; }
    public void setIsPermanent(Boolean isPermanent) { this.isPermanent = isPermanent; }
}