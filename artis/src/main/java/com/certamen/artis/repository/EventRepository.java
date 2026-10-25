package com.certamen.artis.repository;

import com.certamen.artis.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório responsável pela comunicação com a tabela 'event'.
 * Usamos o Spring Data JPA para não precisarmos escrever SQL na mão
 * para as operações básicas de CRUD.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    /**
     * Busca um evento pelo ID único que vem do equipamento (ex: EVT-1001).
     * O Spring já entende esse nome de método e gera a query automaticamente.
     *
     * @param eventId O identificador único do evento na fábrica.
     * @return Um Optional contendo o evento, se ele existir no banco.
     */
    Optional<Event> findByEventId(String eventId);

    /**
     * Verifica se um evento já foi processado anteriormente.
     * É uma alternativa mais leve ao findByEventId quando só queremos saber se existe.
     *
     * @param eventId O identificador único do evento na fábrica.
     * @return true se o evento já estiver registrado, false caso contrário.
     */
    boolean existsByEventId(String eventId);
}