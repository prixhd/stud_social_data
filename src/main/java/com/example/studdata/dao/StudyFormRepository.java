package com.example.studdata.dao;

import com.example.studdata.model.StudyForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyFormRepository extends JpaRepository<StudyForm, Long> {
    Optional<StudyForm> findByName(String name);
}