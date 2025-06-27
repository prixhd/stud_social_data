package com.example.studdata.controller;

import com.example.studdata.model.Student;
import com.example.studdata.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.List;

@Controller

public class HttpController {
    @Autowired
    private StudentService service;

    @GetMapping("/")
    public String homeStudentPage(Model model) {
        LocalDate currentDate = LocalDate.now();
        List<Student> students = service.findStudentsByIssuanceEndDateBeforeOrEqual(currentDate);

        long countStudents = service.count();
        long countStudentsByIssuanceEndDateAfter = students.size();


        model.addAttribute("title", "Студенческая база данных");
        model.addAttribute("pageActiveHome", "nav-link active");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link");
        model.addAttribute("countStudents", countStudents);
        model.addAttribute("countStudentsByIssuanceEndDateAfter", countStudentsByIssuanceEndDateAfter);

        return "students-home";
    }
}
