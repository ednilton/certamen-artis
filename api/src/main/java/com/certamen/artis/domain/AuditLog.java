package com.certamen.artis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * É o nosso "livro de registros" (tabela 'audit_log').
 * Toda vez que chegar um evento do tipo QUALITY, a gente vai gravar uma linha aqui
 * pra deixar tudo rastreável e auditável.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Entity
@Table(name = "audit_log")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditLog {

    /**
     * ID único desse registro de auditoria (UUID), gerado pelo Hibernate.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Qual foi o evento que gerou essa auditoria.
     * Amarra com o campo 'eventId' da classe Event.
     */
    @Column(name = "event_id", nullable = false, length = 255)
    private String eventId;

    /**
     * O que aconteceu (por exemplo, "AUDITORIA_GERADA").
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    /**
     * Quando esse log foi criado. Não deixa atualizar depois, óbvio.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Preenche a data de criação automaticamente, caso não tenha sido informada.
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
}