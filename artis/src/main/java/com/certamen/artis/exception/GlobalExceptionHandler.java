package com.certamen.artis.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Intercepta as exceções da aplicação e transforma em respostas JSON padronizadas
 * usando o ProblemDetail (RFC 7807).
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata a tentativa de enviar um evento duplicado (Retorna HTTP 409 - Conflict).
     */
    @ExceptionHandler(DuplicateEventException.class)
    public ProblemDetail handleDuplicateEvent(DuplicateEventException ex) {
        log.warn("Requisição rejeitada: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Evento Duplicado");
        problemDetail.setType(URI.create("https://api.certamen.artis/errors/duplicate-event"));
        return problemDetail;
    }

    /**
     * Trata erros de validação do payload (Retorna HTTP 400 - Bad Request).
     * Exemplo: mandar um JSON sem eventId ou sem eventTime.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Erro de validação no payload: {}", errors);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.");
        problemDetail.setTitle("Erro de Validação");
        problemDetail.setProperty("invalid_fields", errors);
        return problemDetail;
    }

    /**
     * Fallback para qualquer outro erro inesperado (Retorna HTTP 500).
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Erro inesperado no servidor", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado.");
        problemDetail.setTitle("Erro Interno");
        return problemDetail;
    }
}