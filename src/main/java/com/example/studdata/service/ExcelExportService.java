package com.example.studdata.service;

import com.example.studdata.config.AppConfig;
import com.example.studdata.model.Student;
import com.example.studdata.util.ApplicationMetrics;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelExportService {

    private final AppConfig appConfig;
    private final ApplicationMetrics applicationMetrics;

    public String exportToExcel(List<Student> students) {
        Timer.Sample sample = null;
        String filePath = null;

        try {
            // Запускаем таймер метрик
            sample = applicationMetrics.startExportTimer();
            log.info("🔄 Начинаем экспорт Excel файла");

            // Валидация входных данных
            validateInput(students);
            log.info("✅ Валидация входных данных прошла успешно");

            // Создаем путь к файлу
            filePath = createExportPath();
            log.info("✅ Путь к файлу создан: {}", filePath);

            // Создаем Excel файл
            createExcelFile(students, filePath);
            log.info("✅ Excel файл успешно создан: {}", filePath);

            // Проверяем что файл действительно создался
            validateCreatedFile(filePath);
            log.info("✅ Файл прошел валидацию");

            // Увеличиваем счетчик экспортов
            applicationMetrics.incrementExportCounter();
            log.info("📊 Экспортировано {} записей", students.size());

            return filePath;

        } catch (IllegalArgumentException e) {
            log.error("❌ Ошибка валидации данных: {}", e.getMessage());
            throw new RuntimeException("Ошибка валидации: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("❌ Ошибка ввода/вывода при создании Excel файла: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка создания файла: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Неожиданная ошибка при экспорте Excel: {}", e.getMessage(), e);
            // Пытаемся удалить частично созданный файл
            cleanupFile(filePath);
            throw new RuntimeException("Неожиданная ошибка при экспорте: " + e.getMessage(), e);
        } finally {
            if (sample != null) {
                applicationMetrics.stopExportTimer(sample);
            }
        }
    }

    private void validateInput(List<Student> students) {
        if (students == null) {
            throw new IllegalArgumentException("Список студентов не может быть null");
        }

        if (students.isEmpty()) {
            throw new IllegalArgumentException("Список студентов не может быть пустым");
        }

        if (students.size() > appConfig.getMaxExportRecords()) {
            throw new IllegalArgumentException(
                    String.format("Слишком много записей для экспорта: %d. Максимум: %d",
                            students.size(), appConfig.getMaxExportRecords())
            );
        }

        log.debug("Валидация прошла успешно для {} студентов", students.size());
    }

    private String createExportPath() throws IOException {
        try {
            String userHome = System.getProperty("user.home");
            if (userHome == null || userHome.trim().isEmpty()) {
                throw new IOException("Не удалось определить домашнюю директорию пользователя");
            }

            String[] pathParts = appConfig.getExportPath().split("/");
            Path exportDir = Paths.get(userHome, pathParts);

            // Создаем директорию если не существует
            try {
                Files.createDirectories(exportDir);
                log.debug("Директория для экспорта создана/проверена: {}", exportDir.toAbsolutePath());
            } catch (IOException e) {
                throw new IOException("Не удалось создать директорию для экспорта: " + exportDir, e);
            }

            // Создаем имя файла с timestamp
            String timestamp;
            try {
                timestamp = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern(appConfig.getDateFormat())
                );
            } catch (Exception e) {
                log.warn("Ошибка форматирования даты, используем default: {}", e.getMessage());
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            }

            String fileName = String.format("students_export_%s.xlsx", timestamp);
            Path filePath = exportDir.resolve(fileName);

            return filePath.toString();

        } catch (Exception e) {
            log.error("Ошибка при создании пути к файлу: {}", e.getMessage(), e);
            throw new IOException("Не удалось создать путь к файлу", e);
        }
    }

    private void createExcelFile(List<Student> students, String filePath) throws IOException {
        Workbook workbook = null;
        FileOutputStream fileOut = null;

        try {
            // Создаем workbook
            workbook = new XSSFWorkbook();
            log.debug("Workbook создан");

            // Создаем содержимое Excel
            createExcelContent(workbook, students);
            log.debug("Содержимое Excel создано");

            // Сохраняем файл
            fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);
            log.debug("Файл записан на диск: {}", filePath);

        } catch (IOException e) {
            log.error("Ошибка при работе с файлом: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при создании Excel: {}", e.getMessage(), e);
            throw new IOException("Ошибка создания Excel содержимого", e);
        } finally {
            // Закрываем ресурсы в правильном порядке
            try {
                if (fileOut != null) {
                    fileOut.flush();
                    fileOut.close();
                    log.debug("FileOutputStream закрыт");
                }
            } catch (IOException e) {
                log.warn("Ошибка при закрытии FileOutputStream: {}", e.getMessage());
            }

            try {
                if (workbook != null) {
                    workbook.close();
                    log.debug("Workbook закрыт");
                }
            } catch (IOException e) {
                log.warn("Ошибка при закрытии Workbook: {}", e.getMessage());
            }
        }
    }

    private void createExcelContent(Workbook workbook, List<Student> students) {
        try {
            Sheet sheet = workbook.createSheet("Студенты");
            log.debug("Лист создан");

            // Создаем заголовок
            createHeader(workbook, sheet);
            log.debug("Заголовок создан");

            // Заполняем данные
            fillData(sheet, students);
            log.debug("Данные заполнены");

            // Автоматически подгоняем размер колонок
            autoSizeColumns(sheet);
            log.debug("Размеры колонок настроены");

        } catch (Exception e) {
            log.error("Ошибка при создании содержимого Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка создания Excel содержимого", e);
        }
    }

    private void createHeader(Workbook workbook, Sheet sheet) {
        try {
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Фамилия", "Имя", "Отчество", "Курс", "Факультет",
                    "Форма обучения", "Стипендия", "Номер приказа",
                    "Дата окончания выдачи", "Окончание срока основания", "Основание"
            };

            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < headers.length; i++) {
                try {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                } catch (Exception e) {
                    log.warn("Ошибка при создании ячейки заголовка {}: {}", i, e.getMessage());
                    // Продолжаем создание остальных ячеек
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при создании заголовка: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка создания заголовка", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        try {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);

            // Добавляем границы и цвет фона
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            return headerStyle;

        } catch (Exception e) {
            log.error("Ошибка при создании стиля заголовка: {}", e.getMessage(), e);
            // Возвращаем базовый стиль
            return workbook.createCellStyle();
        }
    }

    private void fillData(Sheet sheet, List<Student> students) {
        try {
            int rowNum = 1;
            for (int i = 0; i < students.size(); i++) {
                try {
                    Student student = students.get(i);
                    if (student != null) {
                        Row row = sheet.createRow(rowNum++);
                        fillStudentRow(row, student);
                    } else {
                        log.warn("Студент с индексом {} равен null, пропускаем", i);
                    }
                } catch (Exception e) {
                    log.warn("Ошибка при обработке студента с индексом {}: {}", i, e.getMessage());
                    // Продолжаем обработку следующих студентов
                }
            }

        } catch (Exception e) {
            log.error("Ошибка при заполнении данных: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка заполнения данных", e);
        }
    }

    private void fillStudentRow(Row row, Student student) {
        try {
            int cellNum = 0;

            // Безопасно заполняем каждую ячейку
            safeSetCellValue(row, cellNum++, student.getId());
            safeSetCellValue(row, cellNum++, nullSafe(student.getLastName()));
            safeSetCellValue(row, cellNum++, nullSafe(student.getFirstName()));
            safeSetCellValue(row, cellNum++, nullSafe(student.getMiddleName()));
            safeSetCellValue(row, cellNum++, student.getCourse());
            safeSetCellValue(row, cellNum++,
                    student.getFaculty() != null ? student.getFaculty().getName() : "");
            safeSetCellValue(row, cellNum++,
                    student.getStudyForm() != null ? student.getStudyForm().getName() : "");
            safeSetCellValue(row, cellNum++,
                    student.getScholarship() != null ? student.getScholarship().getName() : "");
            safeSetCellValue(row, cellNum++, nullSafe(student.getOrderNumber()));
            safeSetCellValue(row, cellNum++,
                    student.getIssuanceEndDate() != null ? student.getIssuanceEndDate().toString() : "");

            // Обработка срока основания
            if (Boolean.TRUE.equals(student.getIsPermanent())) {
                safeSetCellValue(row, cellNum++, "Бессрочно");
            } else {
                safeSetCellValue(row, cellNum++,
                        student.getFoundationEndDate() != null ?
                                student.getFoundationEndDate().toString() : "");
            }

            safeSetCellValue(row, cellNum,
                    student.getFoundation() != null ? student.getFoundation().getName() : "");

        } catch (Exception e) {
            log.warn("Ошибка при заполнении строки студента ID {}: {}",
                    student != null ? student.getId() : "null", e.getMessage());
        }
    }

    private void safeSetCellValue(Row row, int cellIndex, Object value) {
        try {
            Cell cell = row.createCell(cellIndex);
            if (value == null) {
                cell.setCellValue("");
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        } catch (Exception e) {
            log.warn("Ошибка при установке значения ячейки [{}]: {}", cellIndex, e.getMessage());
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        try {
            for (int i = 0; i < 12; i++) {
                try {
                    sheet.autoSizeColumn(i);
                    // Устанавливаем максимальную ширину колонки
                    if (sheet.getColumnWidth(i) > 6000) {
                        sheet.setColumnWidth(i, 6000);
                    }
                } catch (Exception e) {
                    log.warn("Ошибка при автоматическом изменении размера колонки {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Ошибка при автоматическом изменении размеров колонок: {}", e.getMessage());
        }
    }

    private void validateCreatedFile(String filePath) throws IOException {
        try {
            Path path = Paths.get(filePath);

            if (!Files.exists(path)) {
                throw new IOException("Файл не был создан: " + filePath);
            }

            long fileSize = Files.size(path);
            if (fileSize == 0) {
                throw new IOException("Создан пустой файл: " + filePath);
            }

            if (fileSize < 1024) { // Менее 1KB - подозрительно мало для Excel файла
                log.warn("Размер файла подозрительно мал: {} байт", fileSize);
            }

            log.debug("Файл прошел валидацию. Размер: {} байт", fileSize);

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Ошибка при валидации файла: " + e.getMessage(), e);
        }
    }

    private void cleanupFile(String filePath) {
        if (filePath != null) {
            try {
                Path path = Paths.get(filePath);
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("Временный файл удален: {}", filePath);
                }
            } catch (Exception e) {
                log.warn("Не удалось удалить временный файл {}: {}", filePath, e.getMessage());
            }
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}