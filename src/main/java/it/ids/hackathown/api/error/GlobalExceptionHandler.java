package it.ids.hackathown.api.error;

import it.ids.hackathown.api.dto.response.ApiResponse;
import it.ids.hackathown.api.dto.response.ErrorInfo;
import it.ids.hackathown.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse> handleDomainException(
        DomainException ex,
        HttpServletRequest request
    ) {
        return build(ex.getStatus(), ex.getClass().getSimpleName(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .toList();
        return build(HttpStatus.BAD_REQUEST, "ValidationError", "Request validation failed", request, errors);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "BadRequest", ex.getMessage(), request, null);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse> handleMissingHeader(
        MissingRequestHeaderException ex,
        HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "MissingHeader", ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "InternalServerError",
            "Unexpected server error",
            request,
            null
        );
    }

    private ResponseEntity<ApiResponse> build(
        HttpStatus status,
        String error,
        String detail,
        HttpServletRequest request,
        Object errors
    ) {
        ErrorInfo info = new ErrorInfo(status.value(), error, detail, request.getRequestURI(), errors);
        return ResponseEntity.status(status).body(new ApiResponse("ERROR", info));
    }

    private String formatFieldError(FieldError fieldError) {
        String message = fieldError.getDefaultMessage() == null ? "invalid value" : fieldError.getDefaultMessage();
        return fieldError.getField() + ": " + message;
    }
}
