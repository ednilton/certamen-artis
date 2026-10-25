package com.certamen.artis.service;

import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;
import com.certamen.artis.dto.EventRequest;
import com.certamen.artis.exception.DuplicateEventException;
import com.certamen.artis.repository.EventRepository;
import com.certamen.artis.service.handler.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do EventService (sem Spring, sem banco).
 * Repositório e handlers são mocks: aqui validamos só a regra de orquestração.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private static final OffsetDateTime EVENT_TIME =
            OffsetDateTime.of(2026, 5, 20, 10, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventHandler qualityHandler;

    @Mock
    private EventHandler productionHandler;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        when(qualityHandler.getSupportedType()).thenReturn(EventType.QUALITY);
        when(productionHandler.getSupportedType()).thenReturn(EventType.PRODUCTION);

        eventService = new EventService(eventRepository, List.of(qualityHandler, productionHandler));
        eventService.init(); // simula o @PostConstruct
    }

    @Test
    @DisplayName("Deve registrar o evento e delegar ao handler do tipo QUALITY")
    void createEvent_quality_savesAndDelegatesToQualityHandler() {
        when(eventRepository.existsByEventId("EVT-1001")).thenReturn(false);
        when(eventRepository.saveAndFlush(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.createEvent(request("EVT-1001", EventType.QUALITY));

        assertThat(result.getEventId()).isEqualTo("EVT-1001");
        assertThat(result.getEventType()).isEqualTo(EventType.QUALITY);
        verify(qualityHandler).handle(result);
        verify(productionHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve registrar o evento e delegar ao handler do tipo PRODUCTION")
    void createEvent_production_delegatesToProductionHandlerOnly() {
        when(eventRepository.existsByEventId("EVT-2001")).thenReturn(false);
        when(eventRepository.saveAndFlush(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.createEvent(request("EVT-2001", EventType.PRODUCTION));

        verify(productionHandler).handle(result);
        verify(qualityHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve copiar os dados do request e preencher createdAt em UTC")
    void createEvent_mapsRequestAndSetsCreatedAtInUtc() {
        when(eventRepository.existsByEventId("EVT-1002")).thenReturn(false);
        when(eventRepository.saveAndFlush(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        eventService.createEvent(request("EVT-1002", EventType.PRODUCTION));

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).saveAndFlush(captor.capture());
        Event saved = captor.getValue();

        assertThat(saved.getId()).as("id deve ser gerado pelo Hibernate, não pelo service").isNull();
        assertThat(saved.getDescription()).isEqualTo("Descrição de teste");
        assertThat(saved.getEventTime()).isEqualTo(EVENT_TIME);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt().getOffset()).isEqualTo(ZoneOffset.UTC);
    }

    @Test
    @DisplayName("Deve lançar DuplicateEventException quando o eventId já existe (sem salvar nem processar)")
    void createEvent_duplicate_throwsAndDoesNotSave() {
        when(eventRepository.existsByEventId("EVT-1001")).thenReturn(true);

        assertThatThrownBy(() -> eventService.createEvent(request("EVT-1001", EventType.QUALITY)))
                .isInstanceOf(DuplicateEventException.class)
                .hasMessageContaining("EVT-1001");

        verify(eventRepository, never()).saveAndFlush(any());
        verify(qualityHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve converter violação de UNIQUE (requisições concorrentes) em DuplicateEventException")
    void createEvent_uniqueViolation_isTranslatedToDuplicate() {
        when(eventRepository.existsByEventId("EVT-1001")).thenReturn(false);
        when(eventRepository.saveAndFlush(any(Event.class)))
                .thenThrow(integrityViolation("23505")); // unique_violation no PostgreSQL

        assertThatThrownBy(() -> eventService.createEvent(request("EVT-1001", EventType.QUALITY)))
                .isInstanceOf(DuplicateEventException.class);

        verify(qualityHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Outros erros de integridade NÃO devem ser tratados como duplicidade")
    void createEvent_otherIntegrityViolation_isRethrown() {
        when(eventRepository.existsByEventId("EVT-1001")).thenReturn(false);
        when(eventRepository.saveAndFlush(any(Event.class)))
                .thenThrow(integrityViolation("23502")); // not_null_violation no PostgreSQL

        assertThatThrownBy(() -> eventService.createEvent(request("EVT-1001", EventType.QUALITY)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .isNotInstanceOf(DuplicateEventException.class);

        verify(qualityHandler, never()).handle(any());
    }

    @Test
    @DisplayName("Deve listar eventos ordenados por createdAt decrescente")
    void getAllEvents_sortsByCreatedAtDesc() {
        when(eventRepository.findAll(any(Sort.class))).thenReturn(List.of());

        eventService.getAllEvents();

        verify(eventRepository).findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Test
    @DisplayName("Deve falhar na inicialização se houver dois handlers para o mesmo tipo")
    void init_duplicateHandlers_failsFast() {
        EventHandler another = mock(EventHandler.class);
        when(another.getSupportedType()).thenReturn(EventType.QUALITY);

        EventService service = new EventService(eventRepository, List.of(qualityHandler, another));

        assertThatThrownBy(service::init)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("QUALITY");
        verifyNoInteractions(eventRepository);
    }

    private static DataIntegrityViolationException integrityViolation(String sqlState) {
        return new DataIntegrityViolationException("erro de integridade",
                new SQLException("simulado", sqlState));
    }

    // Ponto único para ajustar caso a ordem/assinatura do record EventRequest seja diferente
    private static EventRequest request(String eventId, EventType type) {
        return new EventRequest(eventId, type, "Descrição de teste", EVENT_TIME);
    }
}
