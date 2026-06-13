package com.example.studdata.service;

import com.example.studdata.model.ScholarshipOrder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScholarshipOrderService {

    /**
     * Создать новый приказ (деактивирует предыдущие)
     */
    ScholarshipOrder createOrder(ScholarshipOrder order);

    /**
     * Обновить существующий приказ
     */
    ScholarshipOrder updateOrder(ScholarshipOrder order);

    /**
     * Удалить приказ
     */
    void deleteOrder(Long id);

    /**
     * Найти приказ по ID
     */
    Optional<ScholarshipOrder> findById(Long id);

    /**
     * Найти все приказы студента
     */
    List<ScholarshipOrder> findByStudentId(Long studentId);

    /**
     * Найти активные приказы студента
     */
    List<ScholarshipOrder> findActiveByStudentId(Long studentId);

    /**
     * Найти последний активный приказ студента
     */
    Optional<ScholarshipOrder> findLatestActiveByStudentId(Long studentId);

    /**
     * Найти приказы с истекающим сроком (в течение N дней)
     */
    List<ScholarshipOrder> findExpiringOrders(int days);

    /**
     * Найти истекшие приказы
     */
    List<ScholarshipOrder> findExpiredOrders();

    /**
     * Деактивировать приказ
     */
    void deactivateOrder(Long id);

    void activateOrder(Long orderId);

    /**
     * Деактивировать все приказы студента
     */
    void deactivateAllByStudentId(Long studentId);

    /**
     * Валидация приказа
     */
    void validateOrder(ScholarshipOrder order);
}