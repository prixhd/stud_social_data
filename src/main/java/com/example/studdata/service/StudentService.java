package com.example.studdata.service;

import com.example.studdata.model.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudentService {

    // Основные CRUD операции
    List<Student> findAllStudent();
    Student saveStudent(Student student);
    Student updateStudent(Student student);
    void deleteStudent(Student student);
    void deleteStudentById(Long id);
    boolean existsStudentsById(Long id);
    Optional<Student> findStudentById(Long id);
    long count();

    // Поиск и фильтрация
    List<Student> findStudentsByIds(List<Long> ids);
    List<Student> findStudentsByNameOrOrderNumber(String searchTerm);
    List<Student> findAll(Specification<Student> spec, Sort sort);
    List<Student> findAll(Specification<Student> spec);
    List<Student> findByFacultyId(Long facultyId);
    List<Student> findByCourse(int course);
    Optional<Student> findByOrderNumber(String orderNumber);

    // Поиск по датам
    List<Student> findStudentsByIssuanceEndDateAfter(LocalDate currentDate);
    List<Student> findStudentsByIssuanceEndDateBeforeOrEqual(LocalDate currentDate);
    List<Student> findStudentsWithFoundationEndingInWeek(LocalDate startDate, LocalDate endDate);
    List<Student> findPermanentStudents();
    List<Student> findByCreatedDateBetween(LocalDate startDate, LocalDate endDate);
    List<Student> findStudentsByFoundationEndDateBeforeOrEqual(LocalDate currentDate);


    // Статистика
    long countByFacultyId(Long facultyId);

    // Валидация
//    boolean isOrderNumberUnique(String orderNumber, Long excludeId);
    void validateStudent(Student student);

    // Методы для получения справочников
    List<Faculty> findAllFaculties();
    List<Scholarship> findAllScholarships();
    List<Foundation> findAllFoundations();
    List<StudyForm> findAllStudyForms();

    Faculty findFacultyById(Long id);
    Scholarship findScholarshipById(Long id);
    Foundation findFoundationById(Long id);
    StudyForm findStudyFormById(Long id);
}