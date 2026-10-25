package com.certamen.artis.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Intercepta as exceções da aplicação e transforma em respostas JSON padronizadas
 * usando o ProblemDetail (RFC 9457, antiga RFC 7807).
 *
 * Estende o ResponseEntityExceptionHandler do Spring, que já trata direitinho as exceções
 * padrão do MVC (404 de rota inexistente, 405 de método errado, 415 de content-type etc.).
 * Sem isso, essas situações caíam no handler genérico e viravam 500.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERRORS_BASE_URI = "https://api.certamen.artis/errors/";

    /**
     * Evento duplicado (HTTP 409 - Conflict).
     */
    @ExceptionHandler(DuplicateEventException.class)
    public ProblemDetail handleDuplicateEvent(DuplicateEventException ex) {
        log.warn("Requisição rejeitada: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Evento Duplicado");
        problemDetail.setType(URI.create(ERRORS_BASE_URI + "duplicate-event"));
        return problemDetail;
    }

    /**
     * Erros de Bean Validation no payload (HTTP 400).
     * Exemplo: mandar um JSON sem eventId ou sem eventTime.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        log.warn("Erro de validação no payload: {}", errors);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.");
        problemDetail.setTitle("Erro de Validação");
        problemDetail.setType(URI.create(ERRORS_BASE_URI + "validation"));
        problemDetail.setProperty("invalid_fields", errors);
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * JSON que não dá pra ler (HTTP 400): sintaxe quebrada, tipo de evento que não existe,
     * data em formato inválido etc. Antes isso caía no handler genérico e voltava 500.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        log.warn("Payload ilegível: {}", ex.getMostSpecificCause().getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "O corpo da requisição está malformado ou contém valores inválidos "
                        + "(verifique o JSON, o eventType e o formato da data).");
        problemDetail.setTitle("Requisição Inválida");
        problemDetail.setType(URI.create(ERRORS_BASE_URI + "malformed-request"));
        return ResponseEntity.badRequest().body(problemDetail);
    }

    /**
     * Fallback para qualquer outro erro inesperado (HTTP 500).
     * O detalhe técnico vai só pro log, nunca pra resposta.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Erro inesperado no servidor", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado.");
        problemDetail.setTitle("Erro Interno");
        return problemDetail;
    }
}
