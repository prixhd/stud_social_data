package com.example.studdata.dao;

import com.example.studdata.model.Scholarship;
import com.example.studdata.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {
    Scholarship findByName(String scholarshipName);
}
