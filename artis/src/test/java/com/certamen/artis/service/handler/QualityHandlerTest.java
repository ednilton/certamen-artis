package com.certamen.artis.service.handler;

import com.certamen.artis.domain.AuditLog;
import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;
import com.certamen.artis.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Testes unitários do QualityHandler: todo evento QUALITY gera um registro de auditoria.
 */
@ExtendWith(MockitoExtension.class)
class QualityHandlerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private QualityHandler qualityHandler;

    @Test
    @DisplayName("Deve atender apenas eventos do tipo QUALITY")
    void supportedType_isQuality() {
        assertThat(qualityHandler.getSupportedType()).isEqualTo(EventType.QUALITY);
    }

    @Test
    @DisplayName("Deve gravar um AuditLog amarrado ao eventId com a ação AUDITORIA_GERADA")
    void handle_savesAuditLogForEvent() {
        Event event = Event.builder()
                .eventId("EVT-3001")
                .eventType(EventType.QUALITY)
                .description("Inspeção de qualidade")
                .eventTime(OffsetDateTime.now(ZoneOffset.UTC))
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        qualityHandler.handle(event);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog audit = captor.getValue();

        assertThat(audit.getEventId()).isEqualTo("EVT-3001");
        assertThat(audit.getAction()).isEqualTo(QualityHandler.ACTION_AUDITORIA_GERADA);
        assertThat(audit.getId()).as("id deve ser gerado pelo Hibernate").isNull();
    }
}
