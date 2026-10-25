package com.certamen.artis.controller;

import com.certamen.artis.domain.Event;
import com.certamen.artis.dto.EventRequest;
import com.certamen.artis.dto.EventResponse;
import com.certamen.artis.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST responsável por expor os endpoints da nossa API.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Endpoints para gerenciamento de eventos da fábrica")
public class EventController {

    private final EventService eventService;

    /**
     * Recebe um novo evento da fábrica e o registra no sistema.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo evento", description = "Registra o evento no banco e retorna o objeto salvo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Evento duplicado (Idempotência)",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public EventResponse createEvent(@Valid @RequestBody EventRequest request) {
        Event savedEvent = eventService.createEvent(request);
        return EventResponse.fromEntity(savedEvent);
    }

    /**
     * Retorna a lista de todos os eventos registrados.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista todos os eventos", description = "Retorna uma lista com todos os eventos registrados no banco.")
    public List<EventResponse> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return events.stream()
                .map(EventResponse::fromEntity)
                .toList();
    }
}
