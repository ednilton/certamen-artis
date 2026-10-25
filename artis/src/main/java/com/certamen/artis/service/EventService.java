package com.certamen.artis.service;

import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;
import com.certamen.artis.dto.EventRequest;
import com.certamen.artis.exception.DuplicateEventException;
import com.certamen.artis.repository.EventRepository;
import com.certamen.artis.service.handler.EventHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço central que orquestra o recebimento e processamento dos eventos.
 * Aqui mora a lógica de impedir duplicidade e delegar para as regras específicas.
 *
 * @author Ednilton Rauh
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final List<EventHandler> handlers;

    // Mapa de handlers por tipo, para busca rápida
    private final Map<EventType, EventHandler> handlerMap = new EnumMap<>(EventType.class);

    @PostConstruct
    void init() {
        for (EventHandler handler : handlers) {
            EventHandler previous = handlerMap.put(handler.getSupportedType(), handler);
            if (previous != null) {
                throw new IllegalStateException("Mais de um handler registrado para o tipo "
                        + handler.getSupportedType());
            }
        }
        log.info("Handlers registrados: {}", handlerMap.keySet());
    }

    /**
     * Registra e processa um novo evento.
     *
     * @param request Os dados do evento vindos da requisição.
     * @return O evento salvo no banco de dados.
     * @throws DuplicateEventException Se já existir um evento com o mesmo eventId.
     */
    @Transactional
    public Event createEvent(EventRequest request) {
        log.info("Recebendo evento: {}", request.eventId());

        // 1. Atalho rápido para duplicidade (a garantia real é a constraint UNIQUE no banco)
        if (eventRepository.existsByEventId(request.eventId())) {
            throw duplicate(request.eventId());
        }

        // 2. Monta a entidade (o id é gerado pelo Hibernate via @GeneratedValue)
        Event event = Event.builder()
                .eventId(request.eventId())
                .eventType(request.eventType())
                .description(request.description())
                .eventTime(request.eventTime())
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        // 3. Salva e força o INSERT agora, para capturar violação de UNIQUE aqui
        Event savedEvent;
        try {
            savedEvent = eventRepository.saveAndFlush(event);
        } catch (DataIntegrityViolationException ex) {
            throw duplicate(request.eventId());
        }

        // 4. Executa a regra específica do tipo
        EventHandler handler = handlerMap.get(savedEvent.getEventType());
        if (handler != null) {
            handler.handle(savedEvent);
        } else {
            log.error("Nenhum handler encontrado para o tipo de evento: {}", savedEvent.getEventType());
        }

        return savedEvent;
    }

    /**
     * Lista todos os eventos registrados, do mais recente para o mais antigo.
     *
     * @return Lista de eventos.
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private DuplicateEventException duplicate(String eventId) {
        log.warn("Evento duplicado detectado: {}", eventId);
        return new DuplicateEventException("O evento com ID " + eventId + " já foi processado.");
    }
}