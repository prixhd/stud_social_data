package com.example.studdata.dao;

import com.example.studdata.model.Student;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    void deleteById(Long id);

    // Поиск по ФИО и номеру приказа
    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.middleName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Student> findStudentsByNameOrOrderNumber(@Param("searchTerm") String searchTerm);

    List<Student> findAll(Specification<Student> spec, Sort sort);
    List<Student> findAll(Specification<Student> spec);

    @Query("SELECT s FROM Student s WHERE s.issuanceEndDate <= :currentDate")
    List<Student> findStudentsByIssuanceEndDateBeforeOrEqual(LocalDate currentDate);

    @Query("SELECT s FROM Student s WHERE s.isPermanent = false AND " +
            "s.foundationEndDate BETWEEN :startDate AND :endDate")
    List<Student> findStudentsWithFoundationEndingInWeek(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

}