package com.bcastillo.pokeapiback.api.common;

import com.bcastillo.pokeapiback.domain.exception.PokeApiResourceNotFoundException;
import com.bcastillo.pokeapiback.domain.exception.PokemonAlreadyExistsException;
import com.bcastillo.pokeapiback.domain.exception.PokemonNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PokeApiResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePokeApiResourceNotFound(PokeApiResourceNotFoundException ex,
                                                                           HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PokemonNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePokemonNotFound(PokemonNotFoundException ex,
                                                                    HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PokemonAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handlePokemonAlreadyExists(PokemonAlreadyExistsException ex,
                                                                        HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                      HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                                                                    HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "No resource found", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(), status.getReasonPhrase(), message, request.getRequestURI(), Instant.now());
        return ResponseEntity.status(status).body(body);
    }
}
