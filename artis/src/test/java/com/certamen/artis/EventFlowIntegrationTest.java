package com.certamen.artis;

import com.certamen.artis.repository.AuditLogRepository;
import com.certamen.artis.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de integração ponta a ponta: HTTP, controller, service, handlers e PostgreSQL real
 * (Testcontainers). Cobre as regras do enunciado contra o banco de verdade.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EventFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void cleanDatabase() {
        // audit_log primeiro por causa da FK para event(event_id)
        auditLogRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("PRODUCTION: registra o evento e NÃO gera auditoria")
    void production_isOnlyRegistered() throws Exception {
        postEvent("EVT-P-1", "PRODUCTION", "2026-05-20T10:00:00Z")
                .andExpect(status().isCreated());

        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(auditLogRepository.count()).isZero();
    }

    @Test
    @DisplayName("QUALITY: registra o evento E gera exatamente um registro de auditoria")
    void quality_isRegisteredAndAudited() throws Exception {
        postEvent("EVT-Q-1", "QUALITY", "2026-05-20T10:00:00Z")
                .andExpect(status().isCreated());

        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(auditLogRepository.findAll())
                .singleElement()
                .satisfies(audit -> {
                    assertThat(audit.getEventId()).isEqualTo("EVT-Q-1");
                    assertThat(audit.getAction()).isEqualTo("AUDITORIA_GERADA");
                    assertThat(audit.getCreatedAt()).isNotNull();
                });
    }

    @Test
    @DisplayName("Duplicado: segundo envio retorna 409 e não grava nada a mais")
    void duplicate_returns409AndDoesNotPersistTwice() throws Exception {
        postEvent("EVT-Q-2", "QUALITY", "2026-05-20T10:00:00Z").andExpect(status().isCreated());

        postEvent("EVT-Q-2", "QUALITY", "2026-05-20T10:00:00Z")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("O evento com ID EVT-Q-2 já foi processado."));

        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(auditLogRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fuso horário: -03:00 é convertido e devolvido em UTC")
    void offset_isNormalizedToUtc() throws Exception {
        postEvent("EVT-TZ-1", "PRODUCTION", "2026-05-20T07:00:00-03:00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventTime").value("2026-05-20T10:00:00Z"));
    }

    @Test
    @DisplayName("Payload do enunciado (campo 'timestamp', sem fuso) é aceito e tratado como UTC")
    void specPayload_isAccepted() throws Exception {
        String body = """
                {
                  "eventId": "EVT-1001",
                  "eventType": "PRODUCTION",
                  "description": "Production line started",
                  "timestamp": "2026-05-20T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventTime").value("2026-05-20T10:00:00Z"));
    }

    @Test
    @DisplayName("GET lista os eventos do mais recente para o mais antigo")
    void list_isOrderedByMostRecent() throws Exception {
        postEvent("EVT-OLD", "PRODUCTION", "2026-05-20T10:00:00Z").andExpect(status().isCreated());
        postEvent("EVT-NEW", "PRODUCTION", "2026-05-20T11:00:00Z").andExpect(status().isCreated());

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventId").value("EVT-NEW"))
                .andExpect(jsonPath("$[1].eventId").value("EVT-OLD"));
    }

    private org.springframework.test.web.servlet.ResultActions postEvent(
            String eventId, String type, String eventTime) throws Exception {
        String body = """
                {
                  "eventId": "%s",
                  "eventType": "%s",
                  "description": "Teste de integração",
                  "eventTime": "%s"
                }
                """.formatted(eventId, type, eventTime);

        return mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
