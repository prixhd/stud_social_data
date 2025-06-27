package com.example.studdata.config;

import com.example.studdata.dao.FacultyRepository;
import com.example.studdata.dao.FoundationRepository;
import com.example.studdata.dao.ScholarshipRepository;
import com.example.studdata.dao.StudyFormRepository;
import com.example.studdata.model.Faculty;
import com.example.studdata.model.Foundation;
import com.example.studdata.model.Scholarship;
import com.example.studdata.model.StudyForm;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private ScholarshipRepository scholarshipRepository;

    @Autowired
    private FoundationRepository foundationRepository;

    @Autowired
    private StudyFormRepository studyFormRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🔄 Инициализация базовых данных...");

        if (facultyRepository.count() == 0) {
            String[] faculties = {
                    "Биологический", "Востоковедения", "Физкультурный", "Социальный",
                    "Исторический", "ФИЯ", "ФИиИТ", "Культуры", "Математический",
                    "Психологии и философии", "Физический", "Филологический",
                    "Химический", "Экологии", "Экономики", "Юридический",
                    "Управление", "Колледж"
            };

            for (String facultyName : faculties) {
                facultyRepository.save(new Faculty(facultyName));
            }
            log.info("✅ Добавлено {} факультетов", faculties.length);
        } else {
            if (facultyRepository.findByName("Управление") == null) {
                facultyRepository.save(new Faculty("Управление"));
                log.info("✅ Добавлен факультет: Управление");
            }
            if (facultyRepository.findByName("Колледж") == null) {
                facultyRepository.save(new Faculty("Колледж"));
                log.info("✅ Добавлен факультет: Колледж");
            }
        }

        if (studyFormRepository.count() == 0) {
            studyFormRepository.save(new StudyForm("Бакалавриат"));
            studyFormRepository.save(new StudyForm("Магистратура"));
            log.info("✅ Добавлены формы обучения");
        }

        if (scholarshipRepository.count() == 0) {
            scholarshipRepository.save(new Scholarship("Социальная стипендия"));
            scholarshipRepository.save(new Scholarship("Социальная стипендия в повышенном размере"));
            log.info("✅ Добавлены типы стипендий");
        }

        if (foundationRepository.count() == 0) {
            foundationRepository.save(new Foundation("УСЗН"));
            foundationRepository.save(new Foundation("Инвалидность"));
            foundationRepository.save(new Foundation("Постановление"));
            foundationRepository.save(new Foundation("Ветеран боевых действий"));
            log.info("✅ Добавлены основания");
        } else {
            Foundation oldFoundation = foundationRepository.findByName("Военнослужащий");
            if (oldFoundation != null) {
                oldFoundation.setName("Ветеран боевых действий");
                foundationRepository.save(oldFoundation);
                log.info("✅ Обновлено основание: Военнослужащий -> Ветеран боевых действий");
            } else if (foundationRepository.findByName("Ветеран боевых действий") == null) {
                foundationRepository.save(new Foundation("Ветеран боевых действий"));
                log.info("✅ Добавлено основание: Ветеран боевых действий");
            }
        }

        log.info("🎉 Инициализация базовых данных завершена!");
    }
}