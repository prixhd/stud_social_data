package com.example.studdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto {

    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Size(max = 50, message = "Имя не должно превышать 50 символов")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Size(max = 50, message = "Фамилия не должна превышать 50 символов")
    private String lastName;

    @Size(max = 50, message = "Отчество не должно превышать 50 символов")
    private String middleName;

    @Min(value = 1, message = "Курс должен быть от 1 до 6")
    @Max(value = 6, message = "Курс должен быть от 1 до 6")
    private int course;

    @NotNull(message = "Факультет обязателен")
    private Long facultyId;

    @NotNull(message = "Форма обучения обязательна")
    private Long studyFormId;

    @NotNull(message = "Стипендия обязательна")
    private Long scholarshipId;

    @NotNull(message = "Основание обязательно")
    private Long foundationId;

    @NotBlank(message = "Номер приказа обязателен")
    @Size(max = 100, message = "Номер приказа не должен превышать 100 символов")
    private String orderNumber;

    @NotNull(message = "Дата окончания выдачи обязательна")
    @Future(message = "Дата должна быть в будущем")
    private LocalDate issuanceEndDate;

    private LocalDate foundationEndDate;

    @Builder.Default  // Добавляем эту аннотацию
    private Boolean isPermanent = false;

    // Вспомогательные поля для отображения
    private String facultyName;
    private String studyFormName;
    private String scholarshipName;
    private String foundationName;
}