package com.certamen.artis.service.handler;

import com.certamen.artis.domain.AuditLog;
import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;
import com.certamen.artis.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler para eventos do tipo QUALITY.
 * A regra de negócio exige que, além de registrar o evento principal,
 * a gente gere um log de auditoria amarrado a ele.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QualityHandler implements EventHandler {

    static final String ACTION_AUDITORIA_GERADA = "AUDITORIA_GERADA";

    private final AuditLogRepository auditLogRepository;

    @Override
    public EventType getSupportedType() {
        return EventType.QUALITY;
    }

    @Override
    public void handle(Event event) {
        log.info("Evento de QUALIDADE detectado. Gerando registro de auditoria para o ID: {}", event.getEventId());

        // Monta o registro de auditoria. O id quem gera é o Hibernate (@GeneratedValue)
        // e o createdAt é preenchido em UTC pelo @PrePersist da AuditLog.
        AuditLog auditLog = AuditLog.builder()
                .eventId(event.getEventId())
                .action(ACTION_AUDITORIA_GERADA)
                .build();

        auditLogRepository.save(auditLog);
    }
}