package com.example.studdata.config;

import com.example.studdata.dao.*;
import com.example.studdata.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FacultyRepository facultyRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final FoundationRepository foundationRepository;
    private final StudyFormRepository studyFormRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🚀 Запуск инициализации справочных данных...");

        try {
            initializeFaculties();
            initializeStudyForms();
            initializeScholarships();
            initializeFoundations();

            log.info("✅ Инициализация справочных данных завершена успешно");
        } catch (Exception e) {
            log.error("❌ Ошибка при инициализации справочных данных", e);
        }
    }

    private void initializeFaculties() {
        if (facultyRepository.count() == 0) {
            log.info("Создание факультетов...");

            String[] faculties = {
                    "Биологический", "Востоковедения", "Физкультурный", "Социальный",
                    "Исторический", "ФИЯ", "ФИиИТ", "Культуры", "Математический",
                    "Психологии и философии", "Физический", "Филологический",
                    "Химический", "Экологии", "Экономики", "Юридический",
                    "Управление", "Колледж"
            };

            for (String name : faculties) {
                facultyRepository.save(Faculty.builder().name(name).build());
            }

            log.info("✅ Создано {} факультетов", facultyRepository.count());
        } else {
            log.info("Факультеты уже существуют ({})", facultyRepository.count());
        }
    }

    private void initializeStudyForms() {
        if (studyFormRepository.count() == 0) {
            log.info("Создание форм обучения...");

            String[] studyForms = {
                    "Бакалавриат", "Магистратура", "Специалитет", "Аспирантура"
            };

            for (String name : studyForms) {
                studyFormRepository.save(StudyForm.builder().name(name).build());
            }

            log.info("✅ Создано {} форм обучения", studyFormRepository.count());
        } else {
            log.info("Формы обучения уже существуют ({})", studyFormRepository.count());
        }
    }

    private void initializeScholarships() {
        if (scholarshipRepository.count() == 0) {
            log.info("Создание видов стипендий...");

            String[] scholarships = {
                    "Социальная стипендия",
                    "Социальная стипендия в повышенном размере",
                    "Академическая стипендия",
                    "Именная стипендия",
                    "Стипендия Президента РФ",
                    "Стипендия Правительства РФ"
            };

            for (String name : scholarships) {
                scholarshipRepository.save(Scholarship.builder().name(name).build());
            }

            log.info("✅ Создано {} видов стипендий", scholarshipRepository.count());
        } else {
            log.info("Стипендии уже существуют ({})", scholarshipRepository.count());
        }
    }

    private void initializeFoundations() {
        if (foundationRepository.count() == 0) {
            log.info("Создание оснований...");

            String[] foundations = {
                    "УСЗН", "Инвалидность", "Постановление",
                    "Ветеран боевых действий", "Сирота",
                    "Малообеспеченная семья", "Многодетная семья"
            };

            for (String name : foundations) {
                foundationRepository.save(Foundation.builder().name(name).build());
            }

            log.info("✅ Создано {} оснований", foundationRepository.count());
        } else {
            log.info("Основания уже существуют ({})", foundationRepository.count());
        }
    }
}