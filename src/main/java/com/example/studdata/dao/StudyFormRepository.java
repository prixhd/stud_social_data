package com.example.studdata.dao;

import com.example.studdata.model.StudyForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyFormRepository extends JpaRepository<StudyForm, Long> {
    StudyForm findByName(String studyFormName);
}
