package org.example.graduationproject.analytics.exceptions;

public class AnalyticsException extends RuntimeException {
    
    public AnalyticsException(String message) {
        super(message);
    }
    
    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}

