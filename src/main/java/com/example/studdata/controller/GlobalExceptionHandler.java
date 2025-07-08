package com.example.studdata.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletRequest request, Model model) {
        log.error("Произошла неожиданная ошибка:", ex);

        String url = "Неизвестно";
        if (request != null && request.getRequestURL() != null) {
            url = request.getRequestURL().toString();
        }

        model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Неизвестная ошибка");
        model.addAttribute("url", url);
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", 500);

        return "error";
    }
}