package br.edu.campusgigs.api;

import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> business(ApiException ex) {
        return ResponseEntity.status(ex.status()).body(ApiError.of(ex.code(), ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .distinct().collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> unreadable() {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_JSON", "JSON inválido ou campo com valor incorreto."));
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> invalidParameter() {
        return ResponseEntity.badRequest().body(ApiError.of("INVALID_PARAMETER", "Parâmetro de rota inválido."));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> integrity() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of("DATA_CONFLICT", "Dados já existentes ou incompatíveis com as regras do banco."));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "Não foi possível concluir a operação."));
    }
}
