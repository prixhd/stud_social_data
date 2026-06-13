package com.example.studdata.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request, Model model) {
        log.error("Сущность не найдена: {}", ex.getMessage());

        model.addAttribute("error", ex.getMessage());
        model.addAttribute("url", getRequestUrl(request));
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", 404);

        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request, Model model) {
        log.error("Некорректные данные: {}", ex.getMessage());

        model.addAttribute("error", "Ошибка валидации: " + ex.getMessage());
        model.addAttribute("url", getRequestUrl(request));
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", 400);

        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, HttpServletRequest request, Model model) {
        log.error("Произошла неожиданная ошибка:", ex);

        model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Неизвестная ошибка");
        model.addAttribute("url", getRequestUrl(request));
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", 500);

        return "error";
    }

    private String getRequestUrl(HttpServletRequest request) {
        if (request != null && request.getRequestURL() != null) {
            return request.getRequestURL().toString();
        }
        return "Неизвестно";
    }
}