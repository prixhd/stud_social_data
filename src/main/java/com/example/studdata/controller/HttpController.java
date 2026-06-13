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
import java.util.stream.Collectors;

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

            // Получаем всех студентов
            List<Student> allStudents = service.findAllStudent();
            long countStudents = allStudents.size();

            // Студенты с истекшим сроком ОСНОВАНИЯ (архив)
            List<Student> expiredStudents = allStudents.stream()
                    .filter(s -> {
                        // Бессрочные - не в архиве (проверяем ОБА поля!)
                        if (Boolean.TRUE.equals(s.getIsPermanent()) ||
                                Boolean.TRUE.equals(s.getIsIndefinite())) {
                            return false;
                        }
                        // Срок основания истёк
                        return s.getFoundationEndDate() != null
                                && s.getFoundationEndDate().isBefore(currentDate);
                    })
                    .collect(Collectors.toList());

            // Студенты с окончанием срока ОСНОВАНИЯ в течение 2 недель
            List<Student> expiringStudents = allStudents.stream()
                    .filter(s -> {
                        // Бессрочные - не истекают (проверяем ОБА поля!)
                        if (Boolean.TRUE.equals(s.getIsPermanent()) ||
                                Boolean.TRUE.equals(s.getIsIndefinite())) {
                            return false;
                        }
                        // Срок основания истекает в течение 2 недель
                        return s.getFoundationEndDate() != null
                                && s.getFoundationEndDate().isAfter(currentDate)
                                && s.getFoundationEndDate().isBefore(twoWeeksFromNow);
                    })
                    .collect(Collectors.toList());

            // Актуальные студенты (с действующим сроком основания)
            List<Student> activeStudents = allStudents.stream()
                    .filter(s -> {
                        // Бессрочные - всегда актуальны (проверяем ОБА поля!)
                        if (Boolean.TRUE.equals(s.getIsPermanent()) ||
                                Boolean.TRUE.equals(s.getIsIndefinite())) {
                            return true;
                        }
                        // Срок основания не истёк
                        return s.getFoundationEndDate() != null
                                && s.getFoundationEndDate().isAfter(currentDate);
                    })
                    .collect(Collectors.toList());

            long countExpiredStudents = expiredStudents.size();
            long countExpiringStudents = expiringStudents.size();
            long countActiveStudents = activeStudents.size();

            model.addAttribute("title", "Студенческая база данных");
            model.addAttribute("pageActiveHome", "nav-link active");
            model.addAttribute("pageActiveAdd", "nav-link");
            model.addAttribute("pageActiveShow", "nav-link");

            // Статистика
            model.addAttribute("countStudents", countStudents);
            model.addAttribute("countActiveStudents", countActiveStudents);
            model.addAttribute("countExpiredStudents", countExpiredStudents);
            model.addAttribute("countExpiringStudents", countExpiringStudents);
            model.addAttribute("currentDate", currentDate);

            log.info("Главная страница загружена. Всего: {}, активных: {}, истекших: {}, истекающих: {}",
                    countStudents, countActiveStudents, countExpiredStudents, countExpiringStudents);

            return "students-home";

        } catch (Exception e) {
            log.error("Ошибка при загрузке главной страницы: ", e);
            model.addAttribute("error", "Ошибка при загрузке данных: " + e.getMessage());
            model.addAttribute("timestamp", LocalDate.now());
            return "error";
        }
    }
}