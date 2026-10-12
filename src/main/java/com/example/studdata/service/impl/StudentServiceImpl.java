package com.example.studdata.service.impl;

import com.example.studdata.dao.*;
import com.example.studdata.model.*;
import com.example.studdata.service.StudentService;
import com.example.studdata.util.ApplicationMetrics;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;
    private final ScholarshipOrderRepository scholarshipOrderRepository;
    private final ApplicationMetrics applicationMetrics;

    private final FacultyRepository facultyRepository;

    private final ScholarshipRepository scholarshipRepository;

    private final FoundationRepository foundationRepository;

    private final StudyFormRepository studyFormRepository;


    @Override
    @Cacheable(value = "faculties")
    @Transactional(readOnly = true)
    public List<Faculty> findAllFaculties() {
        log.info("CACHE MISS: загрузка факультетов из MySQL");
        return facultyRepository.findAll();
    }

    @Override
    @Cacheable(value = "scholarships")
    @Transactional(readOnly = true)
    public List<Scholarship> findAllScholarships() {
        log.info("CACHE MISS: загрузка стипендий из MySQL");
        return scholarshipRepository.findAll();
    }

    @Override
    @Cacheable(value = "foundations")
    @Transactional(readOnly = true)
    public List<Foundation> findAllFoundations() {
        log.info("CACHE MISS: загрузка оснований из MySQL");
        return foundationRepository.findAll();
    }

    @Override
    @Cacheable(value = "studyForms")
    @Transactional(readOnly = true)
    public List<StudyForm> findAllStudyForms() {
        log.info("CACHE MISS: загрузка форм обучения из MySQL");
        return studyFormRepository.findAll();
    }

    @Override
    public Faculty findFacultyById(Long id) {
        return facultyRepository.findById(id).orElseThrow();
    }

    @Override
    public Scholarship findScholarshipById(Long id) {
        return scholarshipRepository.findById(id).orElseThrow();
    }

    @Override
    public Foundation findFoundationById(Long id) {
        return foundationRepository.findById(id).orElseThrow();
    }

    @Override
    public StudyForm findStudyFormById(Long id) {
        return studyFormRepository.findById(id).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAllStudent() {
        log.debug("Поиск всех студентов");
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Student> findAll(Pageable pageable) {
        log.debug(
                "Постраничный поиск студентов: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return repository.findAll(pageable);
    }

    @Override
    @Transactional
    public Student saveStudent(Student student) {
        log.info(
                "Сохранение студента: {} {}",
                student.getLastName(),
                student.getFirstName()
        );

        validateStudent(student);

        boolean isNewStudent = student.getId() == null;

        if (student.getIsIndefinite() == null) {
            student.setIsIndefinite(false);
        }

        if (student.getIsPermanent() == null) {
            student.setIsPermanent(false);
        }

        Student savedStudent = repository.save(student);

        if (isNewStudent) {
            createInitialOrderForStudent(savedStudent);
            applicationMetrics.incrementStudentCreatedCounter();

            log.info(
                    "Новый студент сохранен с ID: {}",
                    savedStudent.getId()
            );
        } else {
            applicationMetrics.incrementStudentUpdatedCounter();

            log.info(
                    "Студент обновлен: {}",
                    savedStudent.getId()
            );
        }

        return savedStudent;
    }

    @Override
    @Transactional
    public Student updateStudent(Student student) {
        log.info("Обновление студента с ID: {}", student.getId());

        if (student.getId() == null) {
            throw new IllegalArgumentException(
                    "ID студента не может быть null при обновлении"
            );
        }

        validateStudent(student);

        Student updatedStudent = repository.save(student);
        applicationMetrics.incrementStudentUpdatedCounter();

        log.info("Студент обновлен: {}", updatedStudent.getId());

        return updatedStudent;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findStudentsByFoundationEndDateBeforeOrEqual(
            LocalDate currentDate
    ) {
        log.debug(
                "Поиск студентов с истекшим сроком основания до: {}",
                currentDate
        );

        return repository.findStudentsByFoundationEndDateBeforeOrEqual(
                currentDate
        );
    }

    @Override
    @Transactional
    public void deleteStudent(Student student) {
        log.info(
                "Удаление студента: {} {}",
                student.getLastName(),
                student.getFirstName()
        );

        repository.delete(student);
        applicationMetrics.incrementStudentDeletedCounter();
    }

    @Override
    @Transactional
    public void deleteStudentById(Long id) {
        log.info("Удаление студента по ID: {}", id);

        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(
                    "Студент с ID " + id + " не найден"
            );
        }

        repository.deleteById(id);
        applicationMetrics.incrementStudentDeletedCounter();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsStudentsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findStudentById(Long id) {
        log.debug("Поиск студента по ID: {}", id);
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findStudentsByIds(List<Long> ids) {
        log.debug("Поиск студентов по ID: {}", ids);
        return repository.findAllById(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findStudentsByNameOrOrderNumber(
            String searchTerm
    ) {
        log.debug(
                "Поиск студентов по ФИО или номеру приказа: {}",
                searchTerm
        );

        return repository.findStudentsByNameOrOrderNumber(searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAll(
            Specification<Student> spec,
            Sort sort
    ) {
        return repository.findAll(spec, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAll(Specification<Student> spec) {
        return repository.findAll(spec);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Student> findAll(
            Specification<Student> spec,
            Pageable pageable
    ) {
        log.debug(
                "Постраничная фильтрация студентов: page={}, size={}",
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return repository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findStudentsByIssuanceEndDateAfter(
            LocalDate currentDate
    ) {
        log.debug(
                "Поиск студентов с датой окончания после: {}",
                currentDate
        );

        return repository.findStudentsByIssuanceEndDateAfter(currentDate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findStudentsByIssuanceEndDateBeforeOrEqual(
            LocalDate currentDate
    ) {
        log.debug(
                "Поиск студентов с истекшим сроком до: {}",
                currentDate
        );

        return repository.findStudentsByIssuanceEndDateBeforeOrEqual(
                currentDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findStudentsWithFoundationEndingInWeek(
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info(
                "Поиск студентов с окончанием основания между {} и {}",
                startDate,
                endDate
        );

        List<Student> students =
                repository.findStudentsWithFoundationEndingInWeek(
                        startDate,
                        endDate
                );

        log.info("Найдено студентов: {}", students.size());

        return students;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findByFacultyId(Long facultyId) {
        log.debug("Поиск студентов по факультету: {}", facultyId);
        return repository.findByFacultyId(facultyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findByCourse(int course) {
        log.debug("Поиск студентов по курсу: {}", course);
        return repository.findByCourse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findPermanentStudents() {
        log.debug("Поиск студентов с бессрочным основанием");
        return repository.findPermanentStudents();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Student> findByOrderNumber(String orderNumber) {
        log.debug(
                "Поиск студента по номеру приказа: {}",
                orderNumber
        );

        return repository.findByOrderNumber(orderNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findByCreatedDateBetween(
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.debug(
                "Поиск студентов, созданных между {} и {}",
                startDate,
                endDate
        );

        return repository.findByCreatedDateBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByFacultyId(Long facultyId) {
        return repository.countByFacultyId(facultyId);
    }

    @Override
    public void validateStudent(Student student) {

        if (student.getFirstName() == null
                || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Имя студента обязательно"
            );
        }

        if (student.getLastName() == null
                || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Фамилия студента обязательна"
            );
        }

        if (student.getCourse() < 1 || student.getCourse() > 6) {
            throw new IllegalArgumentException(
                    "Курс должен быть от 1 до 6"
            );
        }

        if (student.getOrderNumber() == null
                || student.getOrderNumber().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Номер приказа обязателен"
            );
        }

        if (student.getIssuanceEndDate() == null) {
            throw new IllegalArgumentException(
                    "Дата окончания выдачи обязательна"
            );
        }

        if (student.getFaculty() == null) {
            throw new IllegalArgumentException(
                    "Факультет обязателен"
            );
        }

        if (student.getStudyForm() == null) {
            throw new IllegalArgumentException(
                    "Форма обучения обязательна"
            );
        }

        if (student.getScholarship() == null) {
            throw new IllegalArgumentException(
                    "Стипендия обязательна"
            );
        }

        if (student.getFoundation() == null) {
            throw new IllegalArgumentException(
                    "Основание обязательно"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudentStatistics getStatistics(
            LocalDate currentDate,
            LocalDate expirationDate
    ) {
        log.debug(
                "Получение агрегированной статистики студентов: {} - {}",
                currentDate,
                expirationDate
        );

        return repository.getStatistics(
                currentDate,
                expirationDate
        );
    }

    private void createInitialOrderForStudent(Student student) {

        List<ScholarshipOrder> existingActiveOrders =
                scholarshipOrderRepository.findActiveByStudentId(
                        student.getId()
                );

        if (!existingActiveOrders.isEmpty()) {
            log.warn(
                    "Студент ID: {} уже имеет {} активных приказов, пропускаем создание",
                    student.getId(),
                    existingActiveOrders.size()
            );
            return;
        }

        if (student.getOrderNumber() == null
                || student.getOrderNumber().trim().isEmpty()) {
            log.warn(
                    "Номер приказа пустой, первичный приказ не создан"
            );
            return;
        }

        try {
            ScholarshipOrder order = ScholarshipOrder.builder()
                    .student(student)
                    .scholarship(student.getScholarship())
                    .foundation(student.getFoundation())
                    .orderNumber(student.getOrderNumber())
                    .issuanceEndDate(student.getIssuanceEndDate())
                    .foundationEndDate(student.getFoundationEndDate())
                    .isPermanent(student.getIsPermanent())
                    .isActive(true)
                    .createdDate(LocalDate.now())
                    .build();

            scholarshipOrderRepository.save(order);

            log.info(
                    "Создан первичный приказ '{}' для студента ID: {}",
                    order.getOrderNumber(),
                    student.getId()
            );

        } catch (Exception e) {
            log.error(
                    "Ошибка создания первичного приказа: {}",
                    e.getMessage()
            );
        }
    }
}