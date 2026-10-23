package br.com.cursohub.shared.api;

import br.com.cursohub.catalog.application.exception.CourseNotFoundException;
import br.com.cursohub.catalog.application.exception.InvalidCatalogFilterException;
import br.com.cursohub.identity.application.exception.*;
import br.com.cursohub.enrollment.application.exception.*;
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
    @ExceptionHandler(CourseUnavailableException.class)
    ResponseEntity<Map<String, Object>> handleUnavailableCourse(CourseUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(EnrollmentConflictException.class)
    ResponseEntity<Map<String, Object>> handleEnrollmentConflict(EnrollmentConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex.getMessage()));
    }

    @ExceptionHandler(InvalidEnrollmentRequestException.class)
    ResponseEntity<Map<String, Object>> handleInvalidEnrollment(InvalidEnrollmentRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler({br.com.cursohub.learning.application.exception.LearningCourseUnavailableException.class,
                       br.com.cursohub.learning.application.exception.LessonUnavailableException.class})
    ResponseEntity<Map<String, Object>> handleLearningNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(br.com.cursohub.learning.application.exception.InvalidLearningRequestException.class)
    ResponseEntity<Map<String, Object>> handleInvalidLearning(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler(br.com.cursohub.instructor.application.exception.InstructorCourseNotFoundException.class)
    ResponseEntity<Map<String, Object>> handleInstructorNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

    @ExceptionHandler(br.com.cursohub.instructor.application.exception.InstructorContentConflictException.class)
    ResponseEntity<Map<String, Object>> handleInstructorConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex.getMessage()));
    }

    @ExceptionHandler(br.com.cursohub.instructor.application.exception.InvalidInstructorContentException.class)
    ResponseEntity<Map<String, Object>> handleInvalidInstructorContent(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler(br.com.cursohub.assets.application.AssetValidationException.class)
    ResponseEntity<Map<String,Object>> handleAssetError(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,Object>> handleInvalidVideoPosition(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body("Posição de vídeo inválida"));
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String,Object>> handleLargeUpload(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body("Limite de upload: 30 MB"));
    }

    @ExceptionHandler(br.com.cursohub.cover.application.CoverValidationException.class)
    ResponseEntity<Map<String,Object>> coverInvalid(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler({DuplicateEmailException.class})
    ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(ex.getMessage()));
    }

    @ExceptionHandler({InvalidCredentialsException.class})
    ResponseEntity<Map<String, Object>> handleBadLogin(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body(ex.getMessage()));
    }

    @ExceptionHandler({InvalidRegistrationException.class})
    ResponseEntity<Map<String, Object>> handleBadRegister(InvalidRegistrationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(ex.getMessage()));
    }

    @ExceptionHandler({AccountUnavailableException.class})
    ResponseEntity<Map<String, Object>> handleAccountUnavailable(AccountUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(ex.getMessage()));
    }

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
