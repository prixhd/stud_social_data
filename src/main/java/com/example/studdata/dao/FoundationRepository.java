package com.example.studdata.dao;

import com.example.studdata.model.Foundation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoundationRepository extends JpaRepository<Foundation, Long> {
    Optional<Foundation> findByName(String name);
}