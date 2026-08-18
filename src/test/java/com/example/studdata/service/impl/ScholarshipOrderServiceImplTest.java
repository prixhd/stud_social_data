package com.example.studdata.service.impl;

import com.example.studdata.dao.ScholarshipOrderRepository;
import com.example.studdata.dao.StudentRepository;
import com.example.studdata.model.Foundation;
import com.example.studdata.model.Scholarship;
import com.example.studdata.model.ScholarshipOrder;
import com.example.studdata.model.Student;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScholarshipOrderServiceImplTest {

    @Mock
    private ScholarshipOrderRepository repository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ScholarshipOrderServiceImpl service;

    private Student student;
    private Scholarship scholarship;
    private Foundation foundation;

    @BeforeEach
    void setUp() {

        scholarship = Scholarship.builder()
                .id(1L)
                .name("Социальная стипендия")
                .build();

        foundation = Foundation.builder()
                .id(1L)
                .name("УСЗН")
                .build();

        student = Student.builder()
                .id(10L)
                .firstName("Иван")
                .lastName("Иванов")
                .course(2)
                .scholarship(scholarship)
                .foundation(foundation)
                .orderNumber("OLD-001")
                .issuanceEndDate(
                        LocalDate.now().plusMonths(2)
                )
                .foundationEndDate(
                        LocalDate.now().plusMonths(1)
                )
                .isPermanent(false)
                .isIndefinite(false)
                .build();
    }

    @Test
    void createOrder_shouldDeactivatePreviousOrdersAndActivateNewOrder() {

        ScholarshipOrder oldOrder =
                createOrder(
                        1L,
                        "OLD-001",
                        true
                );

        ScholarshipOrder newOrder =
                createOrder(
                        null,
                        "NEW-001",
                        false
                );

        when(repository.findActiveByStudentId(10L))
                .thenReturn(List.of(oldOrder));

        when(repository.save(newOrder))
                .thenAnswer(invocation -> {
                    ScholarshipOrder saved =
                            invocation.getArgument(0);

                    saved.setId(2L);
                    return saved;
                });

        ScholarshipOrder result =
                service.createOrder(newOrder);

        assertEquals(2L, result.getId());
        assertTrue(result.getIsActive());

        assertFalse(oldOrder.getIsActive());

        verify(repository)
                .findActiveByStudentId(10L);

        verify(repository)
                .saveAll(List.of(oldOrder));

        verify(repository)
                .save(newOrder);
    }

    @Test
    void createOrder_withoutStudent_shouldThrowValidationException() {

        ScholarshipOrder order =
                createOrder(
                        null,
                        "NEW-001",
                        false
                );

        order.setStudent(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.createOrder(order)
                );

        assertEquals(
                "Студент обязателен",
                exception.getMessage()
        );

        verifyNoInteractions(repository);
    }

    @Test
    void updateOrder_whenOrderDoesNotExist_shouldThrowException() {

        ScholarshipOrder order =
                createOrder(
                        999L,
                        "ORDER-999",
                        true
                );

        when(repository.existsById(999L))
                .thenReturn(false);

        assertThrows(
                EntityNotFoundException.class,
                () -> service.updateOrder(order)
        );

        verify(repository, never())
                .save(any());
    }

    @Test
    void deactivateOrder_shouldSetOrderInactive() {

        ScholarshipOrder order =
                createOrder(
                        1L,
                        "ORDER-001",
                        true
                );

        when(repository.findById(1L))
                .thenReturn(Optional.of(order));

        service.deactivateOrder(1L);

        assertFalse(order.getIsActive());

        verify(repository).save(order);
    }

    @Test
    void activateOrder_shouldDeactivatePreviousAndSynchronizeStudent() {

        Scholarship oldScholarship =
                Scholarship.builder()
                        .id(2L)
                        .name("Академическая")
                        .build();

        Foundation oldFoundation =
                Foundation.builder()
                        .id(2L)
                        .name("Инвалидность")
                        .build();

        ScholarshipOrder currentlyActive =
                ScholarshipOrder.builder()
                        .id(1L)
                        .student(student)
                        .scholarship(oldScholarship)
                        .foundation(oldFoundation)
                        .orderNumber("OLD-001")
                        .issuanceEndDate(
                                LocalDate.now().plusMonths(1)
                        )
                        .foundationEndDate(
                                LocalDate.now().plusWeeks(2)
                        )
                        .isPermanent(false)
                        .isActive(true)
                        .build();

        LocalDate newIssuanceDate =
                LocalDate.now().plusMonths(8);

        LocalDate newFoundationDate =
                LocalDate.now().plusMonths(5);

        ScholarshipOrder orderToActivate =
                ScholarshipOrder.builder()
                        .id(2L)
                        .student(student)
                        .scholarship(scholarship)
                        .foundation(foundation)
                        .orderNumber("NEW-002")
                        .issuanceEndDate(newIssuanceDate)
                        .foundationEndDate(newFoundationDate)
                        .isPermanent(false)
                        .isActive(false)
                        .build();

        when(repository.findById(2L))
                .thenReturn(
                        Optional.of(orderToActivate)
                );

        when(repository.findActiveByStudentId(10L))
                .thenReturn(
                        List.of(currentlyActive)
                );

        service.activateOrder(2L);

        assertFalse(
                currentlyActive.getIsActive()
        );

        assertTrue(
                orderToActivate.getIsActive()
        );

        assertEquals(
                scholarship,
                student.getScholarship()
        );

        assertEquals(
                foundation,
                student.getFoundation()
        );

        assertEquals(
                "NEW-002",
                student.getOrderNumber()
        );

        assertEquals(
                newIssuanceDate,
                student.getIssuanceEndDate()
        );

        assertEquals(
                newFoundationDate,
                student.getFoundationEndDate()
        );

        verify(repository)
                .saveAll(
                        List.of(currentlyActive)
                );

        verify(repository)
                .save(orderToActivate);

        verify(studentRepository)
                .save(student);
    }

    @Test
    void activateOrder_whenOrderDoesNotExist_shouldThrowException() {

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> service.activateOrder(999L)
        );

        verify(studentRepository, never())
                .save(any());
    }

    @Test
    void validateOrder_forPermanentOrder_shouldAllowNullFoundationEndDate() {

        ScholarshipOrder order =
                createOrder(
                        null,
                        "PERMANENT-001",
                        false
                );

        order.setIsPermanent(true);
        order.setFoundationEndDate(null);

        assertDoesNotThrow(
                () -> service.validateOrder(order)
        );
    }

    @Test
    void validateOrder_forNonPermanentOrderWithoutEndDate_shouldThrowException() {

        ScholarshipOrder order =
                createOrder(
                        null,
                        "ORDER-001",
                        false
                );

        order.setIsPermanent(false);
        order.setFoundationEndDate(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.validateOrder(order)
                );

        assertTrue(
                exception.getMessage()
                        .contains("дата окончания основания")
        );
    }

    private ScholarshipOrder createOrder(
            Long id,
            String orderNumber,
            boolean active
    ) {

        return ScholarshipOrder.builder()
                .id(id)
                .student(student)
                .scholarship(scholarship)
                .foundation(foundation)
                .orderNumber(orderNumber)
                .issuanceEndDate(
                        LocalDate.now().plusMonths(6)
                )
                .foundationEndDate(
                        LocalDate.now().plusMonths(3)
                )
                .isPermanent(false)
                .isActive(active)
                .createdDate(LocalDate.now())
                .build();
    }
}