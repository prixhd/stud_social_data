package com.example.studdata.controller;

import com.example.studdata.model.*;
import com.example.studdata.service.ScholarshipOrderService;
import com.example.studdata.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ScholarshipOrderController {

    private final ScholarshipOrderService orderService;
    private final StudentService studentService;

    // ===================================================================
    //  ПРОСМОТР ДЕТАЛЕЙ СТУДЕНТА
    // ===================================================================

    @GetMapping("/students/details/{studentId}")
    public String showStudentDetailsAlt(@PathVariable Long studentId, Model model) {
        return showStudentDetails(studentId, model);
    }

    @GetMapping("/students/{studentId}/details")
    public String showStudentDetails(@PathVariable Long studentId, Model model) {
        log.info("Просмотр деталей студента ID: {}", studentId);

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        List<ScholarshipOrder> ordersList = orderService.findByStudentId(studentId);

        // ✅ ИСПРАВЛЕНИЕ: Поиск активного приказа в сервисе, а не в шаблоне (Thymeleaf не поддерживает Stream API)
        Optional<ScholarshipOrder> activeOrderOpt = orderService.findLatestActiveByStudentId(studentId);
        model.addAttribute("activeOrder", activeOrderOpt.orElse(null));

        model.addAttribute("student", student);
        model.addAttribute("scholarshipOrders", ordersList);
        model.addAttribute("orders", ordersList);
        model.addAttribute("title", "Детали студента: " + student.getFullName());
        model.addAttribute("returnUrl", "/students/show");
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        model.addAttribute("scholarships", studentService.findAllScholarships());
        model.addAttribute("foundations", studentService.findAllFoundations());
        model.addAttribute("faculties", studentService.findAllFaculties());
        model.addAttribute("studyForms", studentService.findAllStudyForms());

        return "details/student-details";
    }

    // ===================================================================
    //  РЕДАКТИРОВАНИЕ СТУДЕНТА ИЗ ДЕТАЛЕЙ
    // ===================================================================

    @GetMapping("/students/details/{studentId}/edit")
    public String editStudentFromDetails(@PathVariable Long studentId, Model model) {
        if (!studentService.existsStudentsById(studentId)) {
            return "redirect:/students/show";
        }

        Student student = studentService.findStudentById(studentId).orElse(null);

        model.addAttribute("student", student);
        model.addAttribute("faculties", studentService.findAllFaculties());
        model.addAttribute("studyForms", studentService.findAllStudyForms());
        model.addAttribute("scholarships", studentService.findAllScholarships());
        model.addAttribute("foundations", studentService.findAllFoundations());
        model.addAttribute("title", "Редактирование студента");
        model.addAttribute("returnUrl", "/students/details/" + studentId);
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link active");
        model.addAttribute("pageActiveShow", "nav-link");

        return "details/student-edit-details";
    }

    @PostMapping("/students/details/{studentId}/edit")
    public String updateStudentFromDetails(@PathVariable Long studentId,
                                           @RequestParam String firstName,
                                           @RequestParam String lastName,
                                           @RequestParam(required = false) String middleName,
                                           @RequestParam int course,
                                           @RequestParam Long facultyId,
                                           @RequestParam Long studyFormId,
                                           @RequestParam(required = false) Long scholarshipId,
                                           @RequestParam(required = false) Long foundationId,
                                           @RequestParam(required = false) String orderNumber,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                                           @RequestParam(required = false) Boolean isPermanent,
                                           @RequestParam(required = false) Boolean isIndefinite,
                                           RedirectAttributes redirectAttributes) {

        try {
            Student student = studentService.findStudentById(studentId).orElseThrow();

            Faculty faculty = studentService.findFacultyById(facultyId);
            StudyForm studyForm = studentService.findStudyFormById(studyFormId);

            student.setFirstName(firstName);
            student.setLastName(lastName);
            student.setMiddleName(middleName);
            student.setCourse(course);
            student.setFaculty(faculty);
            student.setStudyForm(studyForm);

            if (scholarshipId != null) {
                student.setScholarship(studentService.findScholarshipById(scholarshipId));
            }
            if (foundationId != null) {
                student.setFoundation(studentService.findFoundationById(foundationId));
            }

            if (orderNumber != null && !orderNumber.isEmpty()) {
                student.setOrderNumber(orderNumber);
            }
            if (issuanceEndDate != null) {
                student.setIssuanceEndDate(issuanceEndDate);
            }

            boolean newIsPermanent = Boolean.TRUE.equals(isPermanent);
            boolean newIsIndefinite = Boolean.TRUE.equals(isIndefinite);

            if (newIsPermanent || newIsIndefinite) {
                student.setFoundationEndDate(null);
                student.setIsPermanent(true);
                student.setIsIndefinite(newIsIndefinite);
            } else {
                if (foundationEndDate != null) {
                    student.setFoundationEndDate(foundationEndDate);
                } else {
                    if (student.getFoundationEndDate() == null) {
                        student.setFoundationEndDate(student.getIssuanceEndDate());
                    }
                }
                student.setIsPermanent(false);
                student.setIsIndefinite(false);
            }

            studentService.saveStudent(student);
            redirectAttributes.addFlashAttribute("successMessage", "Данные студента обновлены");

        } catch (Exception e) {
            log.error("Ошибка при обновлении студента: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/students/details/" + studentId;
    }

    // ===================================================================
    //  ДОБАВЛЕНИЕ ПРИКАЗА
    // ===================================================================

    @GetMapping("/students/details/{studentId}/order/add")
    public String addOrderFromDetailsAlt(@PathVariable Long studentId, Model model) {
        return addOrderForm(studentId, model);
    }

    @GetMapping("/students/details/{studentId}/add-order")
    public String addOrderAlt(@PathVariable Long studentId, Model model) {
        return addOrderForm(studentId, model);
    }

    @GetMapping("/students/{studentId}/orders/add")
    public String addOrderForm(@PathVariable Long studentId, Model model) {
        log.info("Форма добавления приказа для студента ID: {}", studentId);

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        model.addAttribute("student", student);
        model.addAttribute("scholarships", studentService.findAllScholarships());
        model.addAttribute("foundations", studentService.findAllFoundations());
        model.addAttribute("title", "Добавить приказ для " + student.getFullName());
        model.addAttribute("returnUrl", "/students/details/" + studentId);
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link active");
        model.addAttribute("pageActiveShow", "nav-link");

        return "details/add-order";
    }

    @PostMapping("/students/details/{studentId}/order/add")
    public String saveOrderFromDetailsAlt(@PathVariable Long studentId,
                                          @RequestParam Long scholarshipId,
                                          @RequestParam Long foundationId,
                                          @RequestParam String orderNumber,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                                          @RequestParam(required = false) Boolean isPermanent,
                                          RedirectAttributes redirectAttributes) {
        return saveOrder(studentId, scholarshipId, foundationId, orderNumber,
                issuanceEndDate, foundationEndDate, isPermanent, redirectAttributes);
    }

    @PostMapping("/students/details/{studentId}/add-order")
    public String saveOrderAlt(@PathVariable Long studentId,
                               @RequestParam Long scholarshipId,
                               @RequestParam Long foundationId,
                               @RequestParam String orderNumber,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                               @RequestParam(required = false) Boolean isPermanent,
                               RedirectAttributes redirectAttributes) {
        return saveOrder(studentId, scholarshipId, foundationId, orderNumber,
                issuanceEndDate, foundationEndDate, isPermanent, redirectAttributes);
    }

    @PostMapping("/students/{studentId}/orders/add")
    public String saveOrder(@PathVariable Long studentId,
                            @RequestParam Long scholarshipId,
                            @RequestParam Long foundationId,
                            @RequestParam String orderNumber,
                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                            @RequestParam(required = false) Boolean isPermanent,
                            RedirectAttributes redirectAttributes) {

        log.info("Сохранение нового приказа для студента ID: {}", studentId);

        try {
            Student student = studentService.findStudentById(studentId)
                    .orElseThrow(() -> new RuntimeException("Студент не найден"));

            Scholarship scholarship = studentService.findScholarshipById(scholarshipId);
            Foundation foundation = studentService.findFoundationById(foundationId);

            if (Boolean.TRUE.equals(isPermanent)) {
                foundationEndDate = null;
            }

            ScholarshipOrder order = ScholarshipOrder.builder()
                    .student(student)
                    .scholarship(scholarship)
                    .foundation(foundation)
                    .orderNumber(orderNumber)
                    .issuanceEndDate(issuanceEndDate)
                    .foundationEndDate(foundationEndDate)
                    .isPermanent(isPermanent != null ? isPermanent : false)
                    .isActive(true)
                    .createdDate(LocalDate.now())
                    .build();

            orderService.createOrder(order);

            student.setScholarship(scholarship);
            student.setFoundation(foundation);
            student.setOrderNumber(orderNumber);
            student.setIssuanceEndDate(issuanceEndDate);
            student.setFoundationEndDate(foundationEndDate);
            student.setIsPermanent(isPermanent);

            studentService.updateStudent(student);

            redirectAttributes.addFlashAttribute("successMessage", "Приказ успешно добавлен и установлен как текущий");
            log.info("Приказ '{}' добавлен для студента ID: {}", orderNumber, studentId);

        } catch (Exception e) {
            log.error("Ошибка при сохранении приказа: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
            return "redirect:/students/details/" + studentId + "/add-order";
        }

        return "redirect:/students/details/" + studentId;
    }

    // ===================================================================
    //  РЕДАКТИРОВАНИЕ ПРИКАЗА (ИСПРАВЛЕННЫЙ)
    // ===================================================================

    @GetMapping("/students/details/{studentId}/order/{orderId}/edit")
    public String editOrderFromDetailsAlt(@PathVariable Long studentId,
                                          @PathVariable Long orderId,
                                          Model model) {
        return editOrderForm(studentId, orderId, model);
    }

    @GetMapping("/students/{studentId}/orders/edit/{orderId}")
    public String editOrderForm(@PathVariable Long studentId,
                                @PathVariable Long orderId,
                                Model model) {
        log.info("Форма редактирования приказа ID: {} студента ID: {}", orderId, studentId);

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new RuntimeException("Студент не найден"));

        ScholarshipOrder order = orderService.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Приказ не найден"));

        model.addAttribute("student", student);
        model.addAttribute("order", order);
        model.addAttribute("scholarships", studentService.findAllScholarships());
        model.addAttribute("foundations", studentService.findAllFoundations());
        model.addAttribute("title", "Редактировать приказ");
        model.addAttribute("returnUrl", "/students/details/" + studentId);
        model.addAttribute("pageActiveHome", "nav-link");
        model.addAttribute("pageActiveAdd", "nav-link");
        model.addAttribute("pageActiveShow", "nav-link active");

        return "details/edit-order";
    }

    @PostMapping("/students/details/{studentId}/order/{orderId}/edit")
    public String updateOrderFromDetailsAlt(@PathVariable Long studentId,
                                            @PathVariable Long orderId,
                                            @RequestParam Long scholarshipId,
                                            @RequestParam Long foundationId,
                                            @RequestParam String orderNumber,
                                            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                                            @RequestParam(required = false) Boolean isPermanent,
                                            @RequestParam(required = false) Boolean isActive,
                                            RedirectAttributes redirectAttributes) {
        return updateOrder(studentId, orderId, scholarshipId, foundationId, orderNumber,
                issuanceEndDate, foundationEndDate, isPermanent, isActive, redirectAttributes);
    }

    @PostMapping("/students/{studentId}/orders/edit/{orderId}")
    public String updateOrder(@PathVariable Long studentId,
                              @PathVariable Long orderId,
                              @RequestParam Long scholarshipId,
                              @RequestParam Long foundationId,
                              @RequestParam String orderNumber,
                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate issuanceEndDate,
                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate foundationEndDate,
                              @RequestParam(required = false) Boolean isPermanent,
                              @RequestParam(required = false) Boolean isActive,
                              RedirectAttributes redirectAttributes) {

        log.info("Обновление приказа ID: {} студента ID: {}", orderId, studentId);

        try {
            ScholarshipOrder order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Приказ не найден"));

            Scholarship scholarship = studentService.findScholarshipById(scholarshipId);
            Foundation foundation = studentService.findFoundationById(foundationId);

            if (Boolean.TRUE.equals(isPermanent)) {
                foundationEndDate = null;
            }

            order.setScholarship(scholarship);
            order.setFoundation(foundation);
            order.setOrderNumber(orderNumber);
            order.setIssuanceEndDate(issuanceEndDate);
            order.setFoundationEndDate(foundationEndDate);
            order.setIsPermanent(isPermanent != null ? isPermanent : false);

            // ✅ ИСПРАВЛЕНИЕ: Сохраняем текущий статус, если isActive не передан (null)
            if (isActive != null) {
                order.setIsActive(isActive);
            }
            // Если isActive == null, оставляем старое значение order.getIsActive()

            orderService.updateOrder(order);

            // ✅ ИСПРАВЛЕНИЕ: Если приказ стал активным, деактивируем все остальные приказы студента
            if (Boolean.TRUE.equals(order.getIsActive())) {
                // Деактивируем все другие приказы этого студента
                orderService.deactivateAllByStudentId(studentId);

                // Обновляем данные студента текущими значениями
                Student student = studentService.findStudentById(studentId).orElseThrow();
                student.setScholarship(scholarship);
                student.setFoundation(foundation);
                student.setOrderNumber(orderNumber);
                student.setIssuanceEndDate(issuanceEndDate);
                student.setFoundationEndDate(foundationEndDate);
                student.setIsPermanent(isPermanent);
                studentService.updateStudent(student);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Приказ обновлен");

        } catch (Exception e) {
            log.error("Ошибка: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/students/details/" + studentId;
    }

    // ===================================================================
    //  УДАЛЕНИЕ ПРИКАЗА
    // ===================================================================

    @PostMapping("/students/details/{studentId}/order/{orderId}/delete")
    public String deleteOrderFromDetailsAlt(@PathVariable Long studentId,
                                            @PathVariable Long orderId,
                                            RedirectAttributes redirectAttributes) {
        return deleteOrder(studentId, orderId, redirectAttributes);
    }

    @PostMapping("/students/{studentId}/orders/delete/{orderId}")
    public String deleteOrder(@PathVariable Long studentId,
                              @PathVariable Long orderId,
                              RedirectAttributes redirectAttributes) {
        log.info("Удаление приказа ID: {} студента ID: {}", orderId, studentId);

        try {
            orderService.deleteOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Приказ удален");
        } catch (Exception e) {
            log.error("Ошибка: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/students/details/" + studentId;
    }

    // ===================================================================
    //  ДЕАКТИВАЦИЯ ПРИКАЗА
    // ===================================================================

    @PostMapping("/students/details/{studentId}/order/{orderId}/deactivate")
    public String deactivateOrderFromDetailsAlt(@PathVariable Long studentId,
                                                @PathVariable Long orderId,
                                                RedirectAttributes redirectAttributes) {
        return deactivateOrder(studentId, orderId, redirectAttributes);
    }

    @PostMapping("/students/{studentId}/orders/deactivate/{orderId}")
    public String deactivateOrder(@PathVariable Long studentId,
                                  @PathVariable Long orderId,
                                  RedirectAttributes redirectAttributes) {
        log.info("Деактивация приказа ID: {} студента ID: {}", orderId, studentId);

        try {
            orderService.deactivateOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Приказ деактивирован");
        } catch (Exception e) {
            log.error("Ошибка: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/students/details/" + studentId;
    }

    // ===================================================================
    //  АКТИВАЦИЯ ПРИКАЗА
    // ===================================================================

    @PostMapping("/students/{studentId}/orders/activate/{orderId}")
    public String activateOrder(@PathVariable Long studentId,
                                @PathVariable Long orderId,
                                RedirectAttributes redirectAttributes) {
        log.info("Активация приказа ID: {} для студента ID: {}", orderId, studentId);

        try {
            orderService.activateOrder(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "Приказ активирован и установлен как текущий");
        } catch (Exception e) {
            log.error("Ошибка при активации приказа: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: " + e.getMessage());
        }

        return "redirect:/students/details/" + studentId;
    }
}