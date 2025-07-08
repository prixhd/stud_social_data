package com.example.studdata.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationMetrics {

    private final MeterRegistry meterRegistry;

    private Counter exportCounter;
    private Counter studentCreatedCounter;
    private Counter studentUpdatedCounter;
    private Counter studentDeletedCounter;
    private Timer exportTimer;

    @PostConstruct
    public void init() {
        exportCounter = Counter.builder("students.export.count")
                .description("Number of student exports")
                .register(meterRegistry);

        studentCreatedCounter = Counter.builder("students.created.count")
                .description("Number of students created")
                .register(meterRegistry);

        studentUpdatedCounter = Counter.builder("students.updated.count")
                .description("Number of students updated")
                .register(meterRegistry);

        studentDeletedCounter = Counter.builder("students.deleted.count")
                .description("Number of students deleted")
                .register(meterRegistry);

        exportTimer = Timer.builder("students.export.duration")
                .description("Time taken to export students")
                .register(meterRegistry);

        log.info("Application metrics initialized");
    }

    public void incrementExportCounter() {
        exportCounter.increment();
    }

    public void incrementStudentCreatedCounter() {
        studentCreatedCounter.increment();
    }

    public void incrementStudentUpdatedCounter() {
        studentUpdatedCounter.increment();
    }

    public void incrementStudentDeletedCounter() {
        studentDeletedCounter.increment();
    }

    public Timer.Sample startExportTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopExportTimer(Timer.Sample sample) {
        sample.stop(exportTimer);
    }
}