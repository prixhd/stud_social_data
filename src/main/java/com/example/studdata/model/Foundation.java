package com.example.studdata.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "foundations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Foundation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    public Foundation(String name) {
        this.name = name;
    }
}