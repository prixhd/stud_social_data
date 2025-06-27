package com.example.studdata.service.impl;

import com.example.studdata.dao.StudentRepository;
import com.example.studdata.model.Student;
import com.example.studdata.service.StudentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service

public class StudentServiceImpl implements StudentService {
    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired
    private StudentRepository repository;

    @Override
    @Transactional
    public List<Student> findAllStudent() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Student saveStudent(Student student) {
        return repository.save(student);
    }

    @Override
    @Transactional
    public Student updateStudent(Student student) {
        return repository.save(student);
    }

    @Override
    @Transactional
    public void deleteStudent(Student student) {
        repository.delete(student);
    }

    @Override
    @Transactional
    public void deleteStudentById(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean existsStudentsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public Optional<Student> findStudentById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public List<Student> findStudentsByIds(List<Long> ids) {
        log.info("🔍 Поиск студентов по ID: {}", ids);
        List<Student> students = repository.findAllById(ids);
        log.info("✅ Найдено студентов: {}", students.size());
        return students;
    }

    @Override
    @Transactional
    public List<Student> findStudentsByNameOrOrderNumber(String searchTerm) {
        return repository.findStudentsByNameOrOrderNumber(searchTerm);
    }

    @Override
    @Transactional
    public List<Student> findAll(Specification<Student> spec, Sort sort) {
        return repository.findAll(spec, sort);
    }

    @Override
    @Transactional
    public List<Student> findAll(Specification<Student> spec) {
        return repository.findAll(spec);
    }

    @Override
    @Transactional
    public List<Student> findStudentsByIssuanceEndDateBeforeOrEqual(LocalDate currentDate) {
        return repository.findStudentsByIssuanceEndDateBeforeOrEqual(currentDate);
    }

    @Override
    @Transactional
    public List<Student> findStudentsWithFoundationEndingInWeek(LocalDate startDate, LocalDate endDate) {
        return repository.findStudentsWithFoundationEndingInWeek(startDate, endDate);
    }

    @Override
    @Transactional
    public long count() {
        return repository.count();
    }
}