package com.example.jwt;

import java.util.Map;
import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, String>> badRequest(Exception exception) {
        String message = exception instanceof IllegalArgumentException ? exception.getMessage()
                : "Provide valid JSON with all required fields";
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, String>> invalidToken(JwtException exception) {
        return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
    }
}
