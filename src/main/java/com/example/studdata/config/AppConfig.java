package com.example.studdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class AppConfig {

    @NotBlank
    private String exportPath = "Desktop/exports";

    @NotBlank
    private String dateFormat = "yyyy-MM-dd_HH-mm-ss";

    @Positive
    private int maxExportRecords = 10000;

    private boolean enableMetrics = true;
    private String defaultLanguage = "ru";
}