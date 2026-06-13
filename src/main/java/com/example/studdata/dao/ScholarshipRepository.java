package com.example.studdata.dao;

import com.example.studdata.model.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long> {
    Optional<Scholarship> findByName(String name);
}