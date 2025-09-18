package org.example.graduationproject.analytics.controllers;

import org.example.graduationproject.analytics.exceptions.AnalyticsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class AnalyticsControllerAdvice {
    
    @ExceptionHandler(AnalyticsException.class)
    public ResponseEntity<Map<String, String>> handleAnalyticsException(AnalyticsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Analytics Error");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred during analytics processing");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}






















