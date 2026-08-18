package com.example.studdata.service.impl;

import com.example.studdata.dao.*;
import com.example.studdata.model.*;
import com.example.studdata.util.ApplicationMetrics;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ScholarshipOrderRepository scholarshipOrderRepository;

    @Mock
    private ApplicationMetrics applicationMetrics;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private ScholarshipRepository scholarshipRepository;

    @Mock
    private FoundationRepository foundationRepository;

    @Mock
    private StudyFormRepository studyFormRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Faculty faculty;
    private StudyForm studyForm;
    private Scholarship scholarship;
    private Foundation foundation;

    @BeforeEach
    void setUp() {

        faculty = Faculty.builder()
                .id(1L)
                .name("ФИиИТ")
                .build();

        studyForm = StudyForm.builder()
                .id(1L)
                .name("Бакалавриат")
                .build();

        scholarship = Scholarship.builder()
                .id(1L)
                .name("Социальная стипендия")
                .build();

        foundation = Foundation.builder()
                .id(1L)
                .name("УСЗН")
                .build();
    }

    @Test
    void saveStudent_shouldSaveNewStudentAndCreateInitialOrder() {
        Student student = createValidStudent();
        student.setId(null);

        when(studentRepository.save(student))
                .thenAnswer(invocation -> {
                    Student saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        when(scholarshipOrderRepository.findActiveByStudentId(100L))
                .thenReturn(Collections.emptyList());

        Student result = studentService.saveStudent(student);

        assertNotNull(result);
        assertEquals(100L, result.getId());

        verify(studentRepository).save(student);

        verify(scholarshipOrderRepository)
                .findActiveByStudentId(100L);

        ArgumentCaptor<ScholarshipOrder> orderCaptor =
                ArgumentCaptor.forClass(ScholarshipOrder.class);

        verify(scholarshipOrderRepository)
                .save(orderCaptor.capture());

        ScholarshipOrder createdOrder = orderCaptor.getValue();

        assertEquals(result, createdOrder.getStudent());
        assertEquals(
                student.getOrderNumber(),
                createdOrder.getOrderNumber()
        );
        assertEquals(
                scholarship,
                createdOrder.getScholarship()
        );
        assertEquals(
                foundation,
                createdOrder.getFoundation()
        );
        assertTrue(createdOrder.getIsActive());

        verify(applicationMetrics)
                .incrementStudentCreatedCounter();

        verify(applicationMetrics, never())
                .incrementStudentUpdatedCounter();
    }

    @Test
    void saveStudent_shouldNotCreateSecondActiveOrder() {
        Student student = createValidStudent();
        student.setId(null);

        when(studentRepository.save(student))
                .thenAnswer(invocation -> {
                    Student saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        ScholarshipOrder existingOrder =
                ScholarshipOrder.builder()
                        .id(1L)
                        .student(student)
                        .scholarship(scholarship)
                        .foundation(foundation)
                        .orderNumber("OLD-ORDER")
                        .issuanceEndDate(
                                LocalDate.now().plusMonths(1)
                        )
                        .foundationEndDate(
                                LocalDate.now().plusMonths(1)
                        )
                        .isPermanent(false)
                        .isActive(true)
                        .build();

        when(
                scholarshipOrderRepository
                        .findActiveByStudentId(100L)
        ).thenReturn(List.of(existingOrder));

        studentService.saveStudent(student);

        verify(scholarshipOrderRepository, never())
                .save(any(ScholarshipOrder.class));

        verify(applicationMetrics)
                .incrementStudentCreatedCounter();
    }

    @Test
    void saveStudent_shouldNormalizeNullBooleanValues() {
        Student student = createValidStudent();

        student.setId(null);
        student.setIsPermanent(null);
        student.setIsIndefinite(null);

        when(studentRepository.save(student))
                .thenAnswer(invocation -> {
                    Student saved = invocation.getArgument(0);
                    saved.setId(100L);
                    return saved;
                });

        when(
                scholarshipOrderRepository
                        .findActiveByStudentId(100L)
        ).thenReturn(Collections.emptyList());

        studentService.saveStudent(student);

        assertFalse(student.getIsPermanent());
        assertFalse(student.getIsIndefinite());
    }

    @Test
    void saveStudent_withExistingId_shouldCountAsUpdate() {
        Student student = createValidStudent();
        student.setId(10L);

        when(studentRepository.save(student))
                .thenReturn(student);

        Student result =
                studentService.saveStudent(student);

        assertEquals(student, result);

        verify(applicationMetrics)
                .incrementStudentUpdatedCounter();

        verify(applicationMetrics, never())
                .incrementStudentCreatedCounter();

        verifyNoInteractions(scholarshipOrderRepository);
    }

    @Test
    void updateStudent_shouldSaveValidExistingStudent() {
        Student student = createValidStudent();
        student.setId(10L);

        when(studentRepository.save(student))
                .thenReturn(student);

        Student result =
                studentService.updateStudent(student);

        assertEquals(student, result);

        verify(studentRepository).save(student);

        verify(applicationMetrics)
                .incrementStudentUpdatedCounter();
    }

    @Test
    void updateStudent_withoutId_shouldThrowException() {
        Student student = createValidStudent();
        student.setId(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> studentService.updateStudent(student)
                );

        assertEquals(
                "ID студента не может быть null при обновлении",
                exception.getMessage()
        );

        verify(studentRepository, never())
                .save(any());
    }

    @Test
    void deleteStudentById_shouldDeleteExistingStudent() {
        Long studentId = 10L;

        when(studentRepository.existsById(studentId))
                .thenReturn(true);

        studentService.deleteStudentById(studentId);

        verify(studentRepository)
                .deleteById(studentId);

        verify(applicationMetrics)
                .incrementStudentDeletedCounter();
    }

    @Test
    void deleteStudentById_whenStudentDoesNotExist_shouldThrowException() {
        Long studentId = 999L;

        when(studentRepository.existsById(studentId))
                .thenReturn(false);

        EntityNotFoundException exception =
                assertThrows(
                        EntityNotFoundException.class,
                        () -> studentService.deleteStudentById(studentId)
                );

        assertTrue(
                exception.getMessage().contains("999")
        );

        verify(studentRepository, never())
                .deleteById(anyLong());

        verify(applicationMetrics, never())
                .incrementStudentDeletedCounter();
    }

    @Test
    void findAll_shouldDelegatePaginationToRepository() {
        Pageable pageable =
                PageRequest.of(0, 50);

        Student student = createValidStudent();
        student.setId(1L);

        Page<Student> expectedPage =
                new PageImpl<>(
                        List.of(student),
                        pageable,
                        1
                );

        when(studentRepository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<Student> result =
                studentService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(student, result.getContent().get(0));

        verify(studentRepository)
                .findAll(pageable);
    }

    @Test
    void validateStudent_withValidStudent_shouldNotThrow() {
        Student student = createValidStudent();

        assertDoesNotThrow(
                () -> studentService.validateStudent(student)
        );
    }

    @Test
    void validateStudent_withoutFirstName_shouldThrowException() {
        Student student = createValidStudent();
        student.setFirstName(" ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> studentService.validateStudent(student)
                );

        assertEquals(
                "Имя студента обязательно",
                exception.getMessage()
        );
    }

    @Test
    void validateStudent_withoutLastName_shouldThrowException() {
        Student student = createValidStudent();
        student.setLastName(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> studentService.validateStudent(student)
                );

        assertEquals(
                "Фамилия студента обязательна",
                exception.getMessage()
        );
    }

    @Test
    void validateStudent_withInvalidCourse_shouldThrowException() {
        Student student = createValidStudent();
        student.setCourse(7);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> studentService.validateStudent(student)
                );

        assertEquals(
                "Курс должен быть от 1 до 6",
                exception.getMessage()
        );
    }

    @Test
    void validateStudent_withoutOrderNumber_shouldThrowException() {
        Student student = createValidStudent();
        student.setOrderNumber("");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> studentService.validateStudent(student)
                );

        assertEquals(
                "Номер приказа обязателен",
                exception.getMessage()
        );
    }

    @Test
    void validateStudent_withoutIssuanceEndDate_shouldThrowException() {
        Student student = createValidStudent();
        student.setIssuanceEndDate(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> studentService.validateStudent(student)
                );

        assertEquals(
                "Дата окончания выдачи обязательна",
                exception.getMessage()
        );
    }

    @Test
    void validateStudent_withoutFaculty_shouldThrowException() {
        Student student = createValidStudent();
        student.setFaculty(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> studentService.validateStudent(student)
        );
    }

    @Test
    void validateStudent_withoutStudyForm_shouldThrowException() {
        Student student = createValidStudent();
        student.setStudyForm(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> studentService.validateStudent(student)
        );
    }

    @Test
    void validateStudent_withoutScholarship_shouldThrowException() {
        Student student = createValidStudent();
        student.setScholarship(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> studentService.validateStudent(student)
        );
    }

    @Test
    void validateStudent_withoutFoundation_shouldThrowException() {
        Student student = createValidStudent();
        student.setFoundation(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> studentService.validateStudent(student)
        );
    }

    private Student createValidStudent() {
        return Student.builder()
                .firstName("Иван")
                .lastName("Иванов")
                .middleName("Иванович")
                .course(2)
                .faculty(faculty)
                .studyForm(studyForm)
                .scholarship(scholarship)
                .foundation(foundation)
                .orderNumber("ORDER-001")
                .issuanceEndDate(
                        LocalDate.now().plusMonths(6)
                )
                .foundationEndDate(
                        LocalDate.now().plusMonths(3)
                )
                .isPermanent(false)
                .isIndefinite(false)
                .createdDate(LocalDate.now())
                .build();
    }
}