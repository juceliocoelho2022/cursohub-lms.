package br.com.cursohub.shared.api;

import br.com.cursohub.catalog.application.exception.CourseNotFoundException;
import br.com.cursohub.catalog.application.exception.InvalidCatalogFilterException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiErrorHandler {
    @ExceptionHandler(CourseNotFoundException.class)
    ResponseEntity<Map<String, Object>> handleNotFound(CourseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCatalogFilterException.class)
    ResponseEntity<Map<String, Object>> handleInvalidFilter(InvalidCatalogFilterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body("Parâmetro inválido"));
    }

    private Map<String, Object> body(String message) {
        return Map.of("message", message, "timestamp", Instant.now().toString());
    }
}
