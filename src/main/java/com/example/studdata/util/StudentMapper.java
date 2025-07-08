package com.example.studdata.util;

import com.example.studdata.dto.StudentDto;
import com.example.studdata.model.Student;
import com.example.studdata.model.Faculty;
import com.example.studdata.model.Foundation;
import com.example.studdata.model.Scholarship;
import com.example.studdata.model.StudyForm;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentDto toDto(Student student) {
        if (student == null) {
            return null;
        }

        return StudentDto.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .middleName(student.getMiddleName())
                .course(student.getCourse())
                .facultyId(student.getFaculty() != null ? student.getFaculty().getId() : null)
                .studyFormId(student.getStudyForm() != null ? student.getStudyForm().getId() : null)
                .scholarshipId(student.getScholarship() != null ? student.getScholarship().getId() : null)
                .foundationId(student.getFoundation() != null ? student.getFoundation().getId() : null)
                .orderNumber(student.getOrderNumber())
                .issuanceEndDate(student.getIssuanceEndDate())
                .foundationEndDate(student.getFoundationEndDate())
                .isPermanent(student.getIsPermanent())
                .facultyName(student.getFaculty() != null ? student.getFaculty().getName() : null)
                .studyFormName(student.getStudyForm() != null ? student.getStudyForm().getName() : null)
                .scholarshipName(student.getScholarship() != null ? student.getScholarship().getName() : null)
                .foundationName(student.getFoundation() != null ? student.getFoundation().getName() : null)
                .build();
    }

    public Student toEntity(StudentDto dto, Faculty faculty, StudyForm studyForm,
                            Scholarship scholarship, Foundation foundation) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        student.setId(dto.getId());
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setMiddleName(dto.getMiddleName());
        student.setCourse(dto.getCourse());
        student.setFaculty(faculty);
        student.setStudyForm(studyForm);
        student.setScholarship(scholarship);
        student.setFoundation(foundation);
        student.setOrderNumber(dto.getOrderNumber());
        student.setIssuanceEndDate(dto.getIssuanceEndDate());
        student.setFoundationEndDate(dto.getFoundationEndDate());
        student.setIsPermanent(dto.getIsPermanent());

        return student;
    }
}