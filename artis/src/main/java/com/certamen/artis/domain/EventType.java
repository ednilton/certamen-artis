package com.certamen.artis.domain;

/**
 * Os tipos de evento que a fábrica pode mandar pra gente.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
public enum EventType {

    /**
     * Evento de Produção.
     * Regra: A gente só registra no banco e já era.
     */
    PRODUCTION,

    /**
     * Evento de Qualidade.
     * Regra: A gente registra no banco E também gera um registro de auditoria.
     */
    QUALITY
}