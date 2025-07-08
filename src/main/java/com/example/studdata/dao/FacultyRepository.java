package com.example.studdata.dao;

import com.example.studdata.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByName(String facultyName);
    Optional<Faculty> findFirstByName(String name);
}
