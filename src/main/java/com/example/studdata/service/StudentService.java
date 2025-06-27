package com.example.studdata.service;

import com.example.studdata.model.Student;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    List<Student> findAllStudent();
    Student saveStudent(Student student);
    Student updateStudent(Student student);
    void deleteStudent(Student student);
    void deleteStudentById(Long id);
    boolean existsStudentsById(Long id);
    Optional<Student> findStudentById(Long id);
    List<Student> findStudentsByIds(List<Long> ids);
    List<Student> findStudentsByNameOrOrderNumber(String searchTerm);
    List<Student> findAll(Specification<Student> spec, Sort sort);
    List<Student> findAll(Specification<Student> spec);
    List<Student> findStudentsByIssuanceEndDateBeforeOrEqual(LocalDate currentDate);
    List<Student> findStudentsWithFoundationEndingInWeek(LocalDate startDate, LocalDate endDate);
    long count();
}