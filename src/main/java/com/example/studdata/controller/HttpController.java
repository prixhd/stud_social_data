package com.example.studdata.controller;

import com.example.studdata.dao.StudentStatistics;
import com.example.studdata.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HttpController {

    private final StudentService service;

    @GetMapping("/")
    public String homeStudentPage(Model model) {
        try {
            LocalDate currentDate = LocalDate.now();
            LocalDate twoWeeksFromNow = currentDate.plusWeeks(2);

            StudentStatistics statistics =
                    service.getStatistics(
                            currentDate,
                            twoWeeksFromNow
                    );

            model.addAttribute(
                    "title",
                    "Студенческая база данных"
            );

            model.addAttribute(
                    "pageActiveHome",
                    "nav-link active"
            );

            model.addAttribute(
                    "pageActiveAdd",
                    "nav-link"
            );

            model.addAttribute(
                    "pageActiveShow",
                    "nav-link"
            );

            model.addAttribute(
                    "countStudents",
                    statistics.getTotal()
            );

            model.addAttribute(
                    "countActiveStudents",
                    statistics.getActive()
            );

            model.addAttribute(
                    "countExpiredStudents",
                    statistics.getExpired()
            );

            model.addAttribute(
                    "countExpiringStudents",
                    statistics.getExpiring()
            );

            model.addAttribute(
                    "currentDate",
                    currentDate
            );

            log.info(
                    "Главная страница загружена. " +
                            "Всего: {}, активных: {}, истекших: {}, истекающих: {}",
                    statistics.getTotal(),
                    statistics.getActive(),
                    statistics.getExpired(),
                    statistics.getExpiring()
            );

            return "students-home";

        } catch (Exception e) {

            log.error(
                    "Ошибка при загрузке главной страницы",
                    e
            );

            model.addAttribute(
                    "error",
                    "Ошибка при загрузке данных: " + e.getMessage()
            );

            model.addAttribute(
                    "timestamp",
                    LocalDate.now()
            );

            return "error";
        }
    }
}