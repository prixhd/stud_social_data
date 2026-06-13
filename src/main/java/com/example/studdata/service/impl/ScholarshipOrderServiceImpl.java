package com.example.studdata.service.impl;

import com.example.studdata.dao.ScholarshipOrderRepository;
import com.example.studdata.model.ScholarshipOrder;
import com.example.studdata.model.Student;
import com.example.studdata.service.ScholarshipOrderService;
import com.example.studdata.util.ApplicationMetrics;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScholarshipOrderServiceImpl implements ScholarshipOrderService {

    private final ScholarshipOrderRepository repository;

    @Override
    @Transactional
    public ScholarshipOrder createOrder(ScholarshipOrder order) {
        log.info("Создание нового приказа для студента ID: {}", order.getStudent().getId());

        validateOrder(order);

        // Деактивируем все предыдущие приказы студента
        deactivateAllByStudentId(order.getStudent().getId());

        order.setIsActive(true);
        ScholarshipOrder saved = repository.save(order);

        log.info("Приказ создан с ID: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public ScholarshipOrder updateOrder(ScholarshipOrder order) {
        log.info("Обновление приказа ID: {}", order.getId());

        if (!repository.existsById(order.getId())) {
            throw new EntityNotFoundException("Приказ с ID " + order.getId() + " не найден");
        }

        validateOrder(order);

        ScholarshipOrder updated = repository.save(order);
        log.info("Приказ обновлен: {}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Удаление приказа ID: {}", id);

        // Получаем приказ перед удалением, чтобы проверить статус
        ScholarshipOrder order = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Приказ с ID " + id + " не найден"));

        // Если удаляем активный приказ - логируем (можно добавить очистку полей студента)
        if (Boolean.TRUE.equals(order.getIsActive())) {
            Student student = order.getStudent();
            log.info("Удаляется активный приказ студента ID: {}", student.getId());
        }

        repository.deleteById(id);
        log.info("Приказ удален: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScholarshipOrder> findById(Long id) {
        log.debug("Поиск приказа по ID: {}", id);
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipOrder> findByStudentId(Long studentId) {
        log.debug("Поиск всех приказов студента ID: {}", studentId);
        return repository.findByStudentIdOrderByCreatedDateDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipOrder> findActiveByStudentId(Long studentId) {
        log.debug("Поиск активных приказов студента ID: {}", studentId);
        return repository.findActiveByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScholarshipOrder> findLatestActiveByStudentId(Long studentId) {
        log.debug("Поиск последнего активного приказа студента ID: {}", studentId);
        List<ScholarshipOrder> orders = repository.findLatestActiveByStudentId(studentId);
        return orders.isEmpty() ? Optional.empty() : Optional.of(orders.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipOrder> findExpiringOrders(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        log.debug("Поиск приказов с истекающим сроком (в течение {} дней)", days);
        return repository.findExpiringOrders(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScholarshipOrder> findExpiredOrders() {
        LocalDate currentDate = LocalDate.now();
        log.debug("Поиск истекших приказов");
        return repository.findExpiredOrders(currentDate);
    }

    @Override
    @Transactional
    public void deactivateOrder(Long id) {
        log.info("Деактивация приказа ID: {}", id);

        ScholarshipOrder order = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Приказ с ID " + id + " не найден"));

        order.setIsActive(false);
        repository.save(order);

        log.info("Приказ деактивирован: {}", id);
    }

    @Override
    @Transactional
    public void deactivateAllByStudentId(Long studentId) {
        log.info("Деактивация всех приказов студента ID: {}", studentId);

        List<ScholarshipOrder> activeOrders = repository.findActiveByStudentId(studentId);
        activeOrders.forEach(order -> order.setIsActive(false));
        repository.saveAll(activeOrders);

        log.info("Деактивировано {} приказов", activeOrders.size());
    }

    @Override
    public void validateOrder(ScholarshipOrder order) {
        if (order.getStudent() == null) {
            throw new IllegalArgumentException("Студент обязателен");
        }
        if (order.getScholarship() == null) {
            throw new IllegalArgumentException("Стипендия обязательна");
        }
        if (order.getFoundation() == null) {
            throw new IllegalArgumentException("Основание обязательно");
        }
        if (order.getOrderNumber() == null || order.getOrderNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Номер приказа обязателен");
        }
        if (order.getIssuanceEndDate() == null) {
            throw new IllegalArgumentException("Дата окончания выдачи обязательна");
        }
        if (Boolean.FALSE.equals(order.getIsPermanent()) && order.getFoundationEndDate() == null) {
            throw new IllegalArgumentException("Для не бессрочных приказов дата окончания основания обязательна");
        }
    }



    // ✅ ДОБАВИТЬ НОВЫЙ МЕТОД для активации приказа:
    @Override
    @Transactional
    public void activateOrder(Long orderId) {
        log.info("Активация приказа ID: {}", orderId);

        ScholarshipOrder order = repository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Приказ с ID " + orderId + " не найден"));

        Student student = order.getStudent();

        // Деактивируем все текущие приказы студента
        List<ScholarshipOrder> activeOrders = repository.findActiveByStudentId(student.getId());
        activeOrders.forEach(o -> o.setIsActive(false));
        repository.saveAll(activeOrders);

        // Активируем выбранный
        order.setIsActive(true);
        repository.save(order);

        // Обновляем текущие данные студента в основной таблице
        student.setScholarship(order.getScholarship());
        student.setFoundation(order.getFoundation());
        student.setOrderNumber(order.getOrderNumber());
        student.setIssuanceEndDate(order.getIssuanceEndDate());
        student.setFoundationEndDate(order.getFoundationEndDate());
        student.setIsPermanent(order.getIsPermanent());

        // Сохраняем студента (вам нужно внедрить StudentRepository или использовать сервис)
        // Если нет доступа к StudentRepository здесь, уберите эти строки и обновляйте только в контроллере
    }
}