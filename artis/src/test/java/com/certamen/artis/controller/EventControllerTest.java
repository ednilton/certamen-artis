package com.certamen.artis.controller;

import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;
import com.certamen.artis.exception.DuplicateEventException;
import com.certamen.artis.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web (controller + GlobalExceptionHandler + serialização JSON).
 * O EventService é mockado: aqui validamos contrato HTTP, códigos de status e mensagens de erro.
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private static final String VALID_BODY = """
            {
              "eventId": "EVT-1001",
              "eventType": "PRODUCTION",
              "description": "Production line started",
              "eventTime": "2026-05-20T10:00:00Z"
            }
            """;

    @Test
    @DisplayName("POST válido deve retornar 201 com o evento criado")
    void create_valid_returns201() throws Exception {
        when(eventService.createEvent(any())).thenReturn(sampleEvent("EVT-1001", EventType.PRODUCTION));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("EVT-1001"))
                .andExpect(jsonPath("$.eventType").value("PRODUCTION"))
                .andExpect(jsonPath("$.eventTime").value("2026-05-20T10:00:00Z"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("POST duplicado deve retornar 409 em formato Problem Details")
    void create_duplicate_returns409() throws Exception {
        when(eventService.createEvent(any()))
                .thenThrow(new DuplicateEventException("O evento com ID EVT-1001 já foi processado."));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("O evento com ID EVT-1001 já foi processado."));
    }

    @Test
    @DisplayName("POST com JSON malformado deve retornar 400 (e o service nem é chamado)")
    void create_malformedJson_returns400() throws Exception {
        String malformed = """
                { "eventId": "EVT-1001", "description": "sem fechar aspas }
                """;

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(malformed))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    @DisplayName("POST com tipo de evento inexistente deve retornar 400")
    void create_unknownEventType_returns400() throws Exception {
        String body = VALID_BODY.replace("PRODUCTION", "MAINTENANCE");

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    @DisplayName("POST sem eventId deve retornar 400 (Bean Validation)")
    void create_missingEventId_returns400() throws Exception {
        String body = """
                {
                  "eventType": "PRODUCTION",
                  "description": "Sem ID",
                  "eventTime": "2026-05-20T10:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    @DisplayName("POST com eventId acima de 255 caracteres deve retornar 400 com o campo inválido")
    void create_eventIdTooLong_returns400() throws Exception {
        String body = VALID_BODY.replace("EVT-1001", "X".repeat(256));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_fields.eventId").exists());

        verify(eventService, never()).createEvent(any());
    }

    @Test
    @DisplayName("Rota inexistente deve retornar 404 (e não 500)")
    void unknownRoute_returns404() throws Exception {
        mockMvc.perform(get("/api/nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET deve retornar 200 com a lista de eventos")
    void list_returns200WithEvents() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of(
                sampleEvent("EVT-2", EventType.QUALITY),
                sampleEvent("EVT-1", EventType.PRODUCTION)));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventId").value("EVT-2"))
                .andExpect(jsonPath("$[1].eventId").value("EVT-1"));
    }

    private static Event sampleEvent(String eventId, EventType type) {
        return Event.builder()
                .id(UUID.randomUUID())
                .eventId(eventId)
                .eventType(type)
                .description("Production line started")
                .eventTime(OffsetDateTime.of(2026, 5, 20, 10, 0, 0, 0, ZoneOffset.UTC))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }
}
