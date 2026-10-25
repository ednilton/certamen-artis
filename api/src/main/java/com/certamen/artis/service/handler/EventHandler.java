package com.certamen.artis.service.handler;

import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;

/**
 * Contrato base para o processamento de diferentes tipos de evento.
 * Se amanhã a fábrica inventar um novo tipo de evento, a gente só precisa
 * criar uma nova classe que implemente essa interface.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
public interface EventHandler {

    /**
     * Diz qual tipo de evento esse handler é capaz de processar.
     *
     * @return O tipo do evento (ex: PRODUCTION ou QUALITY).
     */
    EventType getSupportedType();

    /**
     * Executa a regra de negócio específica para o evento.
     *
     * @param event O evento que acabou de ser registrado e precisa ser processado.
     */
    void handle(Event event);
}