package com.example.studdata.controller;

import com.example.studdata.dao.FacultyRepository;
import com.example.studdata.dao.FoundationRepository;
import com.example.studdata.dao.ScholarshipRepository;
import com.example.studdata.dao.StudyFormRepository;
import com.example.studdata.model.*;
import com.example.studdata.service.ExcelExportService;
import com.example.studdata.service.StudentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class StudentController {
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentService studentService;

//    @Autowired
//    private FacultyRepository facultyRepository;
//
//    @Autowired
//    private ScholarshipRepository scholarshipRepository;
//
//    @Autowired
//    private FoundationRepository foundationRepository;
//
//    @Autowired
//    private StudyFormRepository studyFormRepository;

    @Autowired
    private ExcelExportService excelExportService;



    @GetMapping("/show")
    public String findAllStudents(Model model) {
        List<Student> students = studentService.findAllStudent();
        model.addAttribute("students", students);
        model.addAttribute("title", "Список студентов в БД");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        model.addAttribute("appliedFaculty", "");
        model.addAttribute("appliedScholarship", "");
        model.addAttribute("appliedStudyForm", "");
        model.addAttribute("appliedFoundation", "");
        model.addAttribute("appliedCourse", "");
        model.addAttribute("appliedSortColumn", "");
        model.addAttribute("appliedSortDirection", "");

        return "show/students-show";
    }

    @GetMapping("/add")
    public String saveStudentForm(Model model) {
        model.addAttribute("title", "Добавление студента");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link active");
        model.addAttribute("pageActiveShow", "nav-link");
        return "add/students-add";
    }

    @PostMapping("/add")
    public String saveStudent(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String middleName,
                              @RequestParam(required = false) String course, // ИЗМЕНЕНО на String
                              @RequestParam Long facultyId,
                              @RequestParam Long studyFormId,
                              @RequestParam Long scholarshipId,
                              @RequestParam Long foundationId,
                              @RequestParam String orderNumber,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                              @RequestParam(required = false) Boolean isPermanent,
                              Model model) {

        // Обработка пустого курса
        int courseValue = 1; // значение по умолчанию
        if (course != null && !course.trim().isEmpty()) {
            try {
                courseValue = Integer.parseInt(course);
            } catch (NumberFormatException e) {
                courseValue = 1; // если не удалось парсить, ставим 1
            }
        }

        Faculty faculty = studentService.findFacultyById(facultyId);
        StudyForm studyForm = studentService.findStudyFormById(studyFormId);
        Scholarship scholarship = studentService.findScholarshipById(scholarshipId);
        Foundation foundation = studentService.findFoundationById(foundationId);

        if (isPermanent != null && isPermanent) {
            foundationEndDate = null;
        }

        Student student = new Student(
                firstName, lastName, middleName, courseValue, // используем courseValue
                faculty, studyForm, scholarship, foundation,
                orderNumber, issuanceEndDate, foundationEndDate, isPermanent
        );

        // Добавить поле isIndefinite
        student.setIsIndefinite(false); // или установить логику

        model.addAttribute("title", "Добавление студента");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link active");
        model.addAttribute("pageActiveShow", "nav-link");

        studentService.saveStudent(student);
        return "add/students-add";
    }

    @GetMapping("show/check-foundation")
    public String checkFoundationDateOfStudents(Model model) {
        LocalDate currentDate = LocalDate.now();
        List<Student> students = studentService.findStudentsByFoundationEndDateBeforeOrEqual(currentDate);

        model.addAttribute("students", students);
        model.addAttribute("title", "Студенты с просроченным сроком основания");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        return "show/students-show";
    }

    @GetMapping("/edit/{id}")
    public String updateStudent(@PathVariable(value = "id") long id, Model model) {
        if(!studentService.existsStudentsById(id)) {
            return "redirect:/students/show";
        }

        Optional<Student> student = studentService.findStudentById(id);
        model.addAttribute("student", student.orElse(null));
        model.addAttribute("title", "Редактирование студента");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link active");
        model.addAttribute("pageActiveShow", "nav-link");

        return "add/students-edit";
    }

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
                                    Model model) {

        Faculty faculty = studentService.findFacultyById(facultyId);
        StudyForm studyForm = studentService.findStudyFormById(studyFormId);
        Scholarship scholarship = studentService.findScholarshipById(scholarshipId);
        Foundation foundation = studentService.findFoundationById(foundationId);

        if (isPermanent != null && isPermanent) {
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
        student.setIsPermanent(isPermanent);

        studentService.saveStudent(student);
        return "redirect:/students/show";
    }

    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable(value = "id") long id) {
        Student student = studentService.findStudentById(id).orElseThrow();
        studentService.deleteStudent(student);
        return "redirect:/students/show";
    }

    // Обновленный поиск - по ФИО и номеру приказа
    @GetMapping("show/search/{searchTerm}")
    public String searchStudent(@PathVariable(value = "searchTerm") String searchTerm, Model model) {
        List<Student> students = studentService.findStudentsByNameOrOrderNumber(searchTerm);

        model.addAttribute("students", students);
        model.addAttribute("title", "Список студентов в БД");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        return "show/students-show";
    }

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

        // Добавляем фильтры
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

        // Новый фильтр по курсу
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

        model.addAttribute("students", students);
        model.addAttribute("title", "Список студентов в БД");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        // ДОБАВЛЯЕМ ПРИМЕНЕННЫЕ ФИЛЬТРЫ В МОДЕЛЬ
        model.addAttribute("appliedFaculty", facultyId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedScholarship", scholarshipId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedStudyForm", studyFormId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedFoundation", foundationId.map(String::valueOf).orElse(""));
        model.addAttribute("appliedCourse", course.map(String::valueOf).orElse(""));
        model.addAttribute("appliedSortColumn", sortColumn != null ? sortColumn : "");
        model.addAttribute("appliedSortDirection", sortDirection != null ? sortDirection : "");

        return "show/students-show";
    }

    @GetMapping("show/check")
    public String checkDateOfStudents(Model model) {
        LocalDate currentDate = LocalDate.now();
        List<Student> students = studentService.findStudentsByIssuanceEndDateBeforeOrEqual(currentDate);

        model.addAttribute("students", students);
        model.addAttribute("title", "Список студентов в БД");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        return "show/students-show";
    }

    // Новый метод для поиска студентов с окончанием срока через неделю
    @GetMapping("show/week-ending")
    public String findStudentsWeekEnding(Model model) {
        LocalDate currentDate = LocalDate.now();
        LocalDate weekFromNow = currentDate.plusWeeks(1);
        LocalDate weekAgo = currentDate.minusWeeks(1);

        List<Student> students = studentService.findStudentsWithFoundationEndingInWeek(weekAgo, weekFromNow);

        model.addAttribute("students", students);
        model.addAttribute("title", "Студенты с окончанием срока основания в течение недели");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        return "show/students-show";
    }

    @PostMapping("show/deleteStudents")
    public String deleteCheckedStudents(@RequestParam("selectedStudents") List<Long> selectedStudentIds) {
        for (Long studentId : selectedStudentIds) {
            studentService.deleteStudentById(studentId);
        }
        return "redirect:/students/show";
    }

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
            log.info("🔄 Начинаем экспорт в Excel");
            log.info("📋 Параметры фильтров:");
            log.info("   - faculty: {}", facultyId);
            log.info("   - scholarship: {}", scholarshipId);
            log.info("   - studyForm: {}", studyFormId);
            log.info("   - foundation: {}", foundationId);
            log.info("   - course: {}", course);
            log.info("   - sortColumn: {}", sortColumn);
            log.info("   - sortDirection: {}", sortDirection);

            // Применяем те же фильтры что и в методе filter
            Specification<Student> spec = Specification.where(null);

            if (facultyId != null && !facultyId.isEmpty()) {
                try {
                    Long fId = Long.parseLong(facultyId);
                    Faculty faculty = studentService.findFacultyById(fId);
                    if (faculty != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("faculty"), faculty));
                        log.info("✅ Применен фильтр по факультету: {}", faculty.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ Неверный формат ID факультета: {}", facultyId);
                }
            }

            if (scholarshipId != null && !scholarshipId.isEmpty()) {
                try {
                    Long sId = Long.parseLong(scholarshipId);
                    Scholarship scholarship = studentService.findScholarshipById(sId);
                    if (scholarship != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("scholarship"), scholarship));
                        log.info("✅ Применен фильтр по стипендии: {}", scholarship.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ Неверный формат ID стипендии: {}", scholarshipId);
                }
            }

            if (studyFormId != null && !studyFormId.isEmpty()) {
                try {
                    Long sfId = Long.parseLong(studyFormId);
                    StudyForm studyForm = studentService.findStudyFormById(sfId);
                    if (studyForm != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("studyForm"), studyForm));
                        log.info("✅ Применен фильтр по форме обучения: {}", studyForm.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ Неверный формат ID формы обучения: {}", studyFormId);
                }
            }

            if (foundationId != null && !foundationId.isEmpty()) {
                try {
                    Long fId = Long.parseLong(foundationId);
                    Foundation foundation = studentService.findFoundationById(fId);
                    if (foundation != null) {
                        spec = spec.and((root, query, criteriaBuilder) ->
                                criteriaBuilder.equal(root.get("foundation"), foundation));
                        log.info("✅ Применен фильтр по основанию: {}", foundation.getName());
                    }
                } catch (NumberFormatException e) {
                    log.warn("⚠️ Неверный формат ID основания: {}", foundationId);
                }
            }

            if (course != null && !course.isEmpty()) {
                try {
                    Integer courseInt = Integer.parseInt(course);
                    spec = spec.and((root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("course"), courseInt));
                    log.info("✅ Применен фильтр по курсу: {}", courseInt);
                } catch (NumberFormatException e) {
                    log.warn("⚠️ Неверный формат курса: {}", course);
                }
            }

            // Применяем сортировку если указана
            Sort sort = null;
            if (sortColumn != null && !sortColumn.isEmpty() &&
                    sortDirection != null && !sortDirection.isEmpty()) {
                sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
                log.info("✅ Применена сортировка: {} {}", sortColumn, sortDirection);
            }

            // Получаем отфильтрованные данные
            List<Student> students;
            if (sort != null) {
                students = studentService.findAll(spec, sort);
            } else {
                students = studentService.findAll(spec);
            }

            log.info("📊 Найдено студентов для экспорта: {}", students.size());

            // Экспортируем в Excel
            String filePath = excelExportService.exportToExcel(students);
            log.info("✅ Файл Excel создан: {}", filePath);

            File file = new File(filePath);
            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Ошибка при экспорте в Excel: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/export-by-ids")
    public ResponseEntity<FileSystemResource> exportByIds(@RequestParam("studentIds") List<Long> studentIds) {
        try {
            log.info("🔄 Начинаем экспорт по ID студентов");
            log.info("📋 ID студентов для экспорта: {}", studentIds);
            log.info("📊 Количество студентов: {}", studentIds.size());

            // Получаем студентов по их ID
            List<Student> students = studentService.findStudentsByIds(studentIds);

            log.info("✅ Найдено студентов в базе: {}", students.size());

            if (students.isEmpty()) {
                log.warn("⚠️ Не найдено студентов для экспорта");
                return ResponseEntity.notFound().build();
            }

            // Экспортируем в Excel
            String filePath = excelExportService.exportToExcel(students);
            log.info("✅ Файл Excel создан: {}", filePath);

            File file = new File(filePath);
            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Ошибка при экспорте по ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}