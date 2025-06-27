package com.example.studdata.service;

import com.example.studdata.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ExcelExportService {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportService.class);


    public String exportToExcel(List<Student> students) throws IOException {
        log.info("🔄 Начинаем создание Excel файла для {} студентов", students.size());

        String userHome = System.getProperty("user.home");
        Path desktopPath = Paths.get(userHome, "Desktop");
        Path exportDir = desktopPath.resolve("exports");

        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
            log.info("📁 Создана папка для экспорта: {}", exportDir.toAbsolutePath());
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "students_export_" + timestamp + ".xlsx";
        String filePath = exportDir.resolve(fileName).toString();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Студенты");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Фамилия", "Имя", "Отчество", "Курс", "Факультет",
                    "Форма обучения", "Стипендия", "Номер приказа",
                    "Дата окончания выдачи", "Окончание срока основания", "Основание"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getLastName());
                row.createCell(2).setCellValue(student.getFirstName());
                row.createCell(3).setCellValue(student.getMiddleName());
                row.createCell(4).setCellValue(student.getCourse());
                row.createCell(5).setCellValue(student.getFaculty() != null ? student.getFaculty().getName() : "");
                row.createCell(6).setCellValue(student.getStudyForm() != null ? student.getStudyForm().getName() : "");
                row.createCell(7).setCellValue(student.getScholarship() != null ? student.getScholarship().getName() : "");
                row.createCell(8).setCellValue(student.getOrderNumber());
                row.createCell(9).setCellValue(student.getIssuanceEndDate() != null ? student.getIssuanceEndDate().toString() : "");

                if (student.getIsPermanent() != null && student.getIsPermanent()) {
                    row.createCell(10).setCellValue("Бессрочно");
                } else {
                    row.createCell(10).setCellValue(student.getFoundationEndDate() != null ? student.getFoundationEndDate().toString() : "");
                }

                row.createCell(11).setCellValue(student.getFoundation() != null ? student.getFoundation().getName() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }

        log.info("✅ Excel файл успешно создан: {}", filePath);
        log.info("📊 Экспортировано {} записей", students.size());
        return filePath;
    }
}