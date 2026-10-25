package com.certamen.artis.repository;

import com.certamen.artis.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório responsável pela tabela 'audit_log'.
 * Ele vai ser muito utilizado quando recebermos eventos do tipo QUALITY,
 * para gravarmos o histórico de auditoria.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    /**
     * Busca todos os registros de auditoria atrelados a um evento específico.
     * Útil se precisarmos rastrear o histórico de um evento em específico.
     *
     * @param eventId O ID do evento original.
     * @return Uma lista contendo os logs de auditoria daquele evento.
     */
    List<AuditLog> findAllByEventId(String eventId);
}