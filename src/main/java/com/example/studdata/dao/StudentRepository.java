package com.example.studdata.dao;

import com.example.studdata.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>,
        JpaSpecificationExecutor<Student> {

    /**
     * Поиск по ФИО и номеру приказа
     */
    @Query("SELECT s FROM Student s WHERE " +
            "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.middleName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Student> findStudentsByNameOrOrderNumber(@Param("searchTerm") String searchTerm);

    /**
     * Студенты с истекшим сроком выдачи стипендии
     */
    @Query("SELECT s FROM Student s WHERE s.issuanceEndDate <= :currentDate")
    List<Student> findStudentsByIssuanceEndDateBeforeOrEqual(@Param("currentDate") LocalDate currentDate);

    /**
     * Студенты с активным сроком выдачи стипендии
     */
    @Query("SELECT s FROM Student s WHERE s.issuanceEndDate > :currentDate")
    List<Student> findStudentsByIssuanceEndDateAfter(@Param("currentDate") LocalDate currentDate);

    /**
     * Студенты с истекшим сроком основания (не бессрочные)
     */
    @Query("SELECT s FROM Student s WHERE " +
            "s.isPermanent = false AND " +
            "s.isIndefinite = false AND " +
            "s.foundationEndDate IS NOT NULL AND " +
            "s.foundationEndDate <= :currentDate")
    List<Student> findStudentsByFoundationEndDateBeforeOrEqual(@Param("currentDate") LocalDate currentDate);

    /**
     * Студенты с окончанием срока основания в определенный период
     */
    @Query("SELECT s FROM Student s WHERE " +
            "s.isPermanent = false AND " +
            "s.isIndefinite = false AND " +
            "s.foundationEndDate IS NOT NULL AND " +
            "s.foundationEndDate BETWEEN :startDate AND :endDate")
    List<Student> findStudentsWithFoundationEndingInWeek(@Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * Поиск студентов по факультету
     */
    @Query("SELECT s FROM Student s WHERE s.faculty.id = :facultyId")
    List<Student> findByFacultyId(@Param("facultyId") Long facultyId);

    /**
     * Поиск студентов по курсу
     */
    List<Student> findByCourse(int course);

    /**
     * Студенты с бессрочным основанием
     */
    @Query("SELECT s FROM Student s WHERE s.isPermanent = true OR s.isIndefinite = true")
    List<Student> findPermanentStudents();

    /**
     * Поиск по номеру приказа (точное совпадение)
     */
    Optional<Student> findByOrderNumber(String orderNumber);

    /**
     * Количество студентов по факультету
     */
    @Query("SELECT COUNT(s) FROM Student s WHERE s.faculty.id = :facultyId")
    long countByFacultyId(@Param("facultyId") Long facultyId);

    /**
     * Студенты, созданные за период
     */
    @Query("SELECT s FROM Student s WHERE s.createdDate BETWEEN :startDate AND :endDate")
    List<Student> findByCreatedDateBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}