package com.example.studdata.dao;

import com.example.studdata.model.ScholarshipOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScholarshipOrderRepository extends JpaRepository<ScholarshipOrder, Long> {

    /**
     * Найти все приказы студента
     */
    List<ScholarshipOrder> findByStudentIdOrderByCreatedDateDesc(Long studentId);

    /**
     * Найти активные приказы студента
     */
    @Query("SELECT o FROM ScholarshipOrder o WHERE o.student.id = :studentId AND o.isActive = true ORDER BY o.createdDate DESC")
    List<ScholarshipOrder> findActiveByStudentId(@Param("studentId") Long studentId);

    /**
     * Найти последний активный приказ студента
     */
    @Query("SELECT o FROM ScholarshipOrder o WHERE o.student.id = :studentId AND o.isActive = true ORDER BY o.createdDate DESC")
    List<ScholarshipOrder> findLatestActiveByStudentId(@Param("studentId") Long studentId);

    /**
     * Найти приказы с истекающим сроком
     */
    @Query("SELECT o FROM ScholarshipOrder o WHERE " +
            "o.isActive = true AND " +
            "o.isPermanent = false AND " +
            "o.foundationEndDate IS NOT NULL AND " +
            "o.foundationEndDate BETWEEN :startDate AND :endDate")
    List<ScholarshipOrder> findExpiringOrders(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Найти истекшие приказы
     */
    @Query("SELECT o FROM ScholarshipOrder o WHERE " +
            "o.isActive = true AND " +
            "o.isPermanent = false AND " +
            "o.foundationEndDate IS NOT NULL AND " +
            "o.foundationEndDate < :currentDate")
    List<ScholarshipOrder> findExpiredOrders(@Param("currentDate") LocalDate currentDate);

    /**
     * Проверка существования активного приказа с таким номером
     */
    boolean existsByOrderNumberAndIsActiveTrue(String orderNumber);

    /**
     * Количество активных приказов студента
     */
    @Query("SELECT COUNT(o) FROM ScholarshipOrder o WHERE o.student.id = :studentId AND o.isActive = true")
    long countActiveByStudentId(@Param("studentId") Long studentId);

    /**
     * Деактивировать все приказы студента (при создании нового)
     */
    @Query("UPDATE ScholarshipOrder o SET o.isActive = false WHERE o.student.id = :studentId")
    void deactivateAllByStudentId(@Param("studentId") Long studentId);
}