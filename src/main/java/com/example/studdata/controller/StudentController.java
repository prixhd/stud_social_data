package com.example.studdata.controller;

import com.example.studdata.model.*;
import com.example.studdata.service.ExcelExportService;
import com.example.studdata.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;
    private final ExcelExportService excelExportService;

    /**
     * Показать всех студентов
     */
    @GetMapping("/show")
    public String findAllStudents(Model model) {
        List<Student> students = studentService.findAllStudent();

        addCommonAttributes(model, "Список студентов в БД");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");
        clearFilterAttributes(model);

        return "show/students-show";
    }

    /**
     * Форма добавления студента
     */
    @GetMapping("/add")
    public String saveStudentForm(Model model) {
        addCommonAttributes(model, "Добавление студента");
        addReferenceData(model);  // ✅ ДОБАВЛЕНО: справочники
        model.addAttribute("pageActiveAdd", "nav-link active");

        return "add/students-add";
    }

    /**
     * Сохранить нового студента
     */
    @PostMapping("/add")
    public String saveStudent(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String middleName,
                              @RequestParam(required = false) String course,
                              @RequestParam Long facultyId,
                              @RequestParam Long studyFormId,
                              @RequestParam Long scholarshipId,
                              @RequestParam Long foundationId,
                              @RequestParam String orderNumber,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                              @RequestParam(required = false) Boolean isPermanent,
                              @RequestParam(required = false) Boolean isIndefinite,
                              Model model) {

        int courseValue = 1;
        if (course != null && !course.trim().isEmpty()) {
            try {
                courseValue = Integer.parseInt(course);
            } catch (NumberFormatException e) {
                courseValue = 1;
            }
        }

        Faculty faculty = studentService.findFacultyById(facultyId);
        StudyForm studyForm = studentService.findStudyFormById(studyFormId);
        Scholarship scholarship = studentService.findScholarshipById(scholarshipId);
        Foundation foundation = studentService.findFoundationById(foundationId);

        // Если бессрочно или indefinite - убираем дату окончания основания
        if (Boolean.TRUE.equals(isPermanent) || Boolean.TRUE.equals(isIndefinite)) {
            foundationEndDate = null;
        }

        Student student = new Student(
                firstName, lastName, middleName, courseValue,
                faculty, studyForm, scholarship, foundation,
                orderNumber, issuanceEndDate, foundationEndDate, isPermanent
        );

        student.setIsIndefinite(isIndefinite != null ? isIndefinite : false);

        studentService.saveStudent(student);

        addCommonAttributes(model, "Добавление студента");
        addReferenceData(model);
        model.addAttribute("pageActiveAdd", "nav-link active");

        return "add/students-add";
    }

    /**
     * Студенты с просроченным сроком основания (архив)
     */
    @GetMapping("show/check-foundation")
    public String checkFoundationDateOfStudents(Model model) {
        LocalDate currentDate = LocalDate.now();
        List<Student> students = studentService.findStudentsByFoundationEndDateBeforeOrEqual(currentDate);

        addCommonAttributes(model, "Студенты с просроченным сроком основания");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");
        model.addAttribute("activeTab", "archived");  // ✅ Для подсветки вкладки

        return "show/students-show";
    }

    /**
     * Форма редактирования студента
     */
    @GetMapping("/edit/{id}")
    public String updateStudent(@PathVariable(value = "id") long id, Model model) {
        if (!studentService.existsStudentsById(id)) {
            return "redirect:/students/show";
        }

        Optional<Student> student = studentService.findStudentById(id);

        addCommonAttributes(model, "Редактирование студента");
        addReferenceData(model);  // ✅ ДОБАВЛЕНО: справочники
        model.addAttribute("student", student.orElse(null));
        model.addAttribute("pageActiveAdd", "nav-link active");

        return "add/students-edit";
    }

    /**
     * Обновить студента
     */
    @PostMapping("/edit/{id}")
    public String updateEditStudent(@PathVariable(value = "id") long id,
                                    @RequestParam String firstName,
                                    @RequestParam String lastName,
                                    @RequestParam String middleName,
                                    @RequestParam int course,
                                    @RequestParam Long facultyId,
                                    @RequestParam Long studyFormId,
                                    @RequestParam Long scholarshipId,
                                    @RequestParam Long foundationId,
                                    @RequestParam String orderNumber,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                                    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                                    @RequestParam(required = false) Boolean isPermanent,
                                    @RequestParam(required = false) Boolean isIndefinite,
                                    Model model) {

        Faculty faculty = studentService.findFacultyById(facultyId);
        StudyForm studyForm = studentService.findStudyFormById(studyFormId);
        Scholarship scholarship = studentService.findScholarshipById(scholarshipId);
        Foundation foundation = studentService.findFoundationById(foundationId);

        // Если бессрочно или indefinite - убираем дату окончания основания
        if (Boolean.TRUE.equals(isPermanent) || Boolean.TRUE.equals(isIndefinite)) {
            foundationEndDate = null;
        }

        Student student = studentService.findStudentById(id).orElseThrow();
        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setMiddleName(middleName);
        student.setCourse(course);
        student.setFaculty(faculty);
        student.setStudyForm(studyForm);
        student.setScholarship(scholarship);
        student.setFoundation(foundation);
        student.setOrderNumber(orderNumber);
        student.setIssuanceEndDate(issuanceEndDate);
        student.setFoundationEndDate(foundationEndDate);
        student.setIsPermanent(isPermanent != null ? isPermanent : false);
        student.setIsIndefinite(isIndefinite != null ? isIndefinite : false);

        studentService.saveStudent(student);

        return "redirect:/students/show";
    }

    /**
     * Удалить студента
     */
    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable(value = "id") long id) {
        Student student = studentService.findStudentById(id).orElseThrow();
        studentService.deleteStudent(student);
        return "redirect:/students/show";
    }

    /**
     * Поиск по ФИО или номеру приказа
     */
    @GetMapping("show/search/{searchTerm}")
    public String searchStudent(@PathVariable(value = "searchTerm") String searchTerm, Model model) {
        List<Student> students = studentService.findStudentsByNameOrOrderNumber(searchTerm);

        addCommonAttributes(model, "Результаты поиска");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");
        model.addAttribute("searchTerm", searchTerm);

        return "show/students-show";
    }

    /**
     * Фильтрация студентов
     */
    @GetMapping("show/filter")
    public String filterStudents(@RequestParam(value = "faculty", required = false) Optional<Long> facultyId,
                                 @RequestParam(value = "scholarship", required = false) Optional<Long> scholarshipId,
                                 @RequestParam(value = "studyForm", required = false) Optional<Long> studyFormId,
                                 @RequestParam(value = "foundation", required = false) Optional<Long> foundationId,
                                 @RequestParam(value = "course", required = false) Optional<Integer> course,
                                 @RequestParam(value = "sortColumn", required = false) String sortColumn,
                                 @RequestParam(value = "sortDirection", required = false) String sortDirection,
                                 Model model) {

        Specification<Student> spec = Specification.where(null);

        if (facultyId.isPresent()) {
            Faculty faculty = studentService.findFacultyById(facultyId.get());
            if (faculty != null) {
                spec = spec.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("faculty"), faculty));
            }
        }

        if (scholarshipId.isPresent()) {
            Scholarship scholarship = studentService.findScholarshipById(scholarshipId.get());
            if (scholarship != null) {
                spec = spec.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("scholarship"), scholarship));
            }
        }

        if (studyFormId.isPresent()) {
            StudyForm studyForm = studentService.findStudyFormById(studyFormId.get());
            if (studyForm != null) {
                spec = spec.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("studyForm"), studyForm));
            }
        }

        if (foundationId.isPresent()) {
            Foundation foundation = studentService.findFoundationById(foundationId.get());
            if (foundation != null) {
                spec = spec.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("foundation"), foundation));
            }
        }

        if (course.isPresent()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("course"), course.get()));
        }

        Sort sort = null;
        if (sortColumn != null && sortDirection != null && !sortColumn.isEmpty() && !sortDirection.isEmpty()) {
            sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
        }

        List<Student> students;
        if (sort != null) {
            students = studentService.findAll(spec, sort);
        } else {
            students = studentService.findAll(spec);
        }

        addCommonAttributes(model, "Список студентов в БД");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");

        // Сохраняем примененные фильтры
        model.addAttribute("appliedFaculty", facultyId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedScholarship", scholarshipId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedStudyForm", studyFormId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedFoundation", foundationId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedCourse", course.map(String::valueOf).orElse(""));
        model.addAttribute("appliedSortColumn", sortColumn != null ? sortColumn : "");
        model.addAttribute("appliedSortDirection", sortDirection != null ? sortDirection : "");

        return "show/students-show";
    }

    /**
     * Студенты с истекшим сроком выдачи стипендии
     */
    @GetMapping("show/check")
    public String checkDateOfStudents(Model model) {
        LocalDate currentDate = LocalDate.now();
        List<Student> students = studentService.findStudentsByIssuanceEndDateBeforeOrEqual(currentDate);

        addCommonAttributes(model, "Студенты с истекшим сроком выдачи");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");

        return "show/students-show";
    }

    /**
     * Студенты с окончанием срока основания в течение недели
     */
    @GetMapping("show/week-ending")
    public String findStudentsWeekEnding(Model model) {
        LocalDate currentDate = LocalDate.now();
        LocalDate weekFromNow = currentDate.plusWeeks(1);
        LocalDate weekAgo = currentDate.minusWeeks(1);

        List<Student> students = studentService.findStudentsWithFoundationEndingInWeek(weekAgo, weekFromNow);

        addCommonAttributes(model, "Студенты с окончанием срока основания в течение недели");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");
        model.addAttribute("activeTab", "expiring");  // ✅ Для подсветки вкладки

        return "show/students-show";
    }

    /**
     * Массовое удаление студентов
     */
    @PostMapping("show/deleteStudents")
    public String deleteCheckedStudents(@RequestParam("selectedStudents") List<Long> selectedStudentIds) {
        for (Long studentId : selectedStudentIds) {
            studentService.deleteStudentById(studentId);
        }
        return "redirect:/students/show";
    }

    /**
     * Экспорт в Excel с фильтрами
     */
    @GetMapping("/export")
    public ResponseEntity<FileSystemResource> exportToExcel(
            @RequestParam(value = "faculty", required = false) String facultyId,
            @RequestParam(value = "scholarship", required = false) String scholarshipId,
            @RequestParam(value = "studyForm", required = false) String studyFormId,
            @RequestParam(value = "foundation", required = false) String foundationId,
            @RequestParam(value = "course", required = false) String course,
            @RequestParam(value = "sortColumn", required = false) String sortColumn,
            @RequestParam(value = "sortDirection", required = false) String sortDirection) {

        try {
            log.info("Начинаем экспорт в Excel");

            Specification<Student> spec = Specification.where(null);

            // Применяем фильтры
            if (facultyId != null && !facultyId.isEmpty()) {
                try {
                    Long fId = Long.parseLong(facultyId);
                    Faculty faculty = studentService.findFacultyById(fId);
                    if (faculty != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("faculty"), faculty));
                    }
                } catch (NumberFormatException e) {
                    log.warn("Неверный формат ID факультета: {}", facultyId);
                }
            }

            if (scholarshipId != null && !scholarshipId.isEmpty()) {
                try {
                    Long sId = Long.parseLong(scholarshipId);
                    Scholarship scholarship = studentService.findScholarshipById(sId);
                    if (scholarship != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("scholarship"), scholarship));
                    }
                } catch (NumberFormatException e) {
                    log.warn("Неверный формат ID стипендии: {}", scholarshipId);
                }
            }

            if (studyFormId != null && !studyFormId.isEmpty()) {
                try {
                    Long sfId = Long.parseLong(studyFormId);
                    StudyForm studyForm = studentService.findStudyFormById(sfId);
                    if (studyForm != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("studyForm"), studyForm));
                    }
                } catch (NumberFormatException e) {
                    log.warn("Неверный формат ID формы обучения: {}", studyFormId);
                }
            }

            if (foundationId != null && !foundationId.isEmpty()) {
                try {
                    Long fId = Long.parseLong(foundationId);
                    Foundation foundation = studentService.findFoundationById(fId);
                    if (foundation != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("foundation"), foundation));
                    }
                } catch (NumberFormatException e) {
                    log.warn("Неверный формат ID основания: {}", foundationId);
                }
            }

            if (course != null && !course.isEmpty()) {
                try {
                    Integer courseInt = Integer.parseInt(course);
                    spec = spec.and((root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("course"), courseInt));
                } catch (NumberFormatException e) {
                    log.warn("Неверный формат курса: {}", course);
                }
            }

            Sort sort = null;
            if (sortColumn != null && !sortColumn.isEmpty() &&
                    sortDirection != null && !sortDirection.isEmpty()) {
                sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            }

            List<Student> students;
            if (sort != null) {
                students = studentService.findAll(spec, sort);
            } else {
                students = studentService.findAll(spec);
            }

            log.info("Найдено студентов для экспорта: {}", students.size());

            String filePath = excelExportService.exportToExcel(students);
            log.info("Файл Excel создан: {}", filePath);

            File file = new File(filePath);
            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            log.error("Ошибка при экспорте в Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Экспорт выбранных студентов
     */
    @PostMapping("/export-by-ids")
    public ResponseEntity<FileSystemResource> exportByIds(@RequestParam("studentIds") List<Long> studentIds) {
        try {
            log.info("Начинаем экспорт по ID студентов");
            log.info("ID студентов для экспорта: {}", studentIds);

            List<Student> students = studentService.findStudentsByIds(studentIds);

            log.info("Найдено студентов в базе: {}", students.size());

            if (students.isEmpty()) {
                log.warn("Не найдено студентов для экспорта");
                return ResponseEntity.notFound().build();
            }

            String filePath = excelExportService.exportToExcel(students);
            log.info("Файл Excel создан: {}", filePath);

            File file = new File(filePath);
            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            log.error("Ошибка при экспорте по ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =====

    /**
     * Добавить общие атрибуты для всех страниц
     */
    private void addCommonAttributes(Model model, String title) {
        model.addAttribute("title", title);
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link");

        // Добавляем справочники для фильтров/форм
        addReferenceData(model);
    }

    /**
     * Добавить справочные данные (факультеты, стипендии и т.д.)
     */
    private void addReferenceData(Model model) {
        model.addAttribute("faculties", studentService.findAllFaculties());
        model.addAttribute("studyForms", studentService.findAllStudyForms());
        model.addAttribute("scholarships", studentService.findAllScholarships());
        model.addAttribute("foundations", studentService.findAllFoundations());
    }

    /**
     * Очистить атрибуты фильтров
     */
    private void clearFilterAttributes(Model model) {
        model.addAttribute("appliedFaculty", "");
        model.addAttribute("appliedScholarship", "");
        model.addAttribute("appliedStudyForm", "");
        model.addAttribute("appliedFoundation", "");
        model.addAttribute("appliedCourse", "");
        model.addAttribute("appliedSortColumn", "");
        model.addAttribute("appliedSortDirection", "");
    }

    /**
     * Истекающие скоро (из главной страницы и вкладок)
     */
    @GetMapping("show/expiring")
    public String findExpiringStudents(Model model) {
        LocalDate currentDate = LocalDate.now();
        LocalDate twoWeeksFromNow = currentDate.plusWeeks(2);

        List<Student> students = studentService.findStudentsWithFoundationEndingInWeek(currentDate, twoWeeksFromNow);

        addCommonAttributes(model, "Студенты с истекающим сроком основания");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");
        model.addAttribute("activeTab", "expiring");

        return "show/students-show";
    }

    /**
     * Архив - истекшие (из главной страницы и вкладок)
     */
    @GetMapping("show/archived")
    public String findArchivedStudents(Model model) {
        LocalDate currentDate = LocalDate.now();

        List<Student> students = studentService.findStudentsByFoundationEndDateBeforeOrEqual(currentDate);

        addCommonAttributes(model, "Архив - студенты с истекшим сроком основания");
        model.addAttribute("students", students);
        model.addAttribute("pageActiveShow", "nav-link active");
        model.addAttribute("activeTab", "archived");

        return "show/students-show";
    }
}