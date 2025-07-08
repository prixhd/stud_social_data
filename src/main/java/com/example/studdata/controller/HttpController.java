package com.example.studdata.controller;

import com.example.studdata.model.Student;
import com.example.studdata.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HttpController {

    private final StudentService service;

    @GetMapping("/")
    public String homeStudentPage(Model model) {
        try {
            LocalDate currentDate = LocalDate.now();

            // Получаем студентов с истекшим сроком выдачи
            List<Student> expiredStudents = service.findStudentsByIssuanceEndDateBeforeOrEqual(currentDate);

            // Получаем студентов с окончанием срока основания на следующей неделе
            LocalDate weekFromNow = currentDate.plusWeeks(1);
            List<Student> weekEndingStudents = service.findStudentsWithFoundationEndingInWeek(currentDate, weekFromNow);

            long countStudents = service.count();
            long countExpiredStudents = expiredStudents.size();
            long countWeekEndingStudents = weekEndingStudents.size();

            model.addAttribute("title", "Студенческая база данных");
            model.addAttribute("pageActiveHome", "nav-link active");
            model.addAttribute("pageActiveAdd", "nav-link");
            model.addAttribute("pageActiveShow", "nav-link");
            model.addAttribute("countStudents", countStudents);
            model.addAttribute("countStudentsByIssuanceEndDateAfter", countExpiredStudents);
            model.addAttribute("countWeekEndingStudents", countWeekEndingStudents);
            model.addAttribute("currentDate", currentDate);
            model.addAttribute("countStudentsByFoundationEndDateExpired",
                    service.findStudentsByFoundationEndDateBeforeOrEqual(LocalDate.now()).size());

            log.info("Главная страница загружена. Всего студентов: {}, с истекшим сроком: {}",
                    countStudents, countExpiredStudents);

            return "students-home";

        } catch (Exception e) {
            log.error("Ошибка при загрузке главной страницы: ", e);
            model.addAttribute("error", "Ошибка при загрузке данных");
            return "error";
        }
    }
}