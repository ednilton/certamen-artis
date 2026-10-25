package com.certamen.artis.service.handler;

import com.certamen.artis.domain.Event;
import com.certamen.artis.domain.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler para eventos do tipo PRODUCTION.
 * A regra de negócio aqui é simples: o evento só precisa ser registrado.
 * O salvamento no banco já foi feito pelo EventService.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Slf4j
@Component
public class ProductionHandler implements EventHandler {

    @Override
    public EventType getSupportedType() {
        return EventType.PRODUCTION;
    }

    @Override
    public void handle(Event event) {
        // Apenas registra um log informativo e não faz mais nada.
        log.info("Evento de PRODUÇÃO processado com sucesso. ID: {}", event.getEventId());
    }
}