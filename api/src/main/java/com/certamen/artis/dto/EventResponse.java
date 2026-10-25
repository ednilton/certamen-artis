package com.certamen.artis.dto;

import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO de saída para representar um evento na resposta da API.
 * Evita expor a entidade JPA diretamente (boa prática de segurança e acoplamento).
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
public record EventResponse(
        UUID id,
        String eventId,
        EventType eventType,
        String description,
        OffsetDateTime eventTime,
        OffsetDateTime createdAt
) {
    /**
     * Método utilitário para converter a Entidade (Event) para DTO (EventResponse).
     *
     * @param event A entidade vinda do banco.
     * @return O DTO pronto para ser serializado em JSON.
     */
    public static EventResponse fromEntity(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventId(),
                event.getEventType(),
                event.getDescription(),
                event.getEventTime(),
                event.getCreatedAt()
        );
    }
}