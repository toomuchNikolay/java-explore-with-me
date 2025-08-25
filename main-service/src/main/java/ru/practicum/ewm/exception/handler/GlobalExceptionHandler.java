package ru.practicum.ewm.exception.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.ewm.exception.custom.AccessDeniedException;
import ru.practicum.ewm.exception.custom.ConflictException;
import ru.practicum.ewm.exception.custom.InvalidParameterException;
import ru.practicum.ewm.exception.custom.NotFoundException;
import ru.practicum.ewm.exception.dto.ApiError;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = String.format("Поле: %s. Ошибка: %s. Значение: %s",
                fieldErrors.getFirst().getField(),
                fieldErrors.getFirst().getDefaultMessage(),
                fieldErrors.getFirst().getRejectedValue());
        List<String> errors = fieldErrors.stream()
                .map(FieldError::toString)
                .toList();
        return buildResponse(HttpStatus.BAD_REQUEST, "Неправильно сделан запрос", errorMessage, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintException(ConstraintViolationException e) {
       List<String> errors = e.getConstraintViolations().stream()
               .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
               .toList();
       return buildResponse(HttpStatus.BAD_REQUEST, "Ошибка в передаваемых параметрах", e.getMessage(), errors);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleDuplicatedException(ConflictException e) {
        return buildResponse(HttpStatus.CONFLICT, "Нарушено ограничение целостности", e.getMessage(), null);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException e) {
        return buildResponse(HttpStatus.NOT_FOUND, "Запрашиваемая сущность не найдена", e.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException e) {
        return buildResponse(HttpStatus.FORBIDDEN, "Доступ запрещен", e.getMessage(), null);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ApiError> handleInvalidParameterException(InvalidParameterException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Неправильно сделан запрос", e.getMessage(), null);
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String reason, String message, List<String> errors) {
        ApiError error = ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .errors(errors != null ? errors : Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
