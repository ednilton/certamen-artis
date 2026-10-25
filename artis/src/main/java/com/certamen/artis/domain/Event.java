package com.certamen.artis.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Representa um evento que chega lá da fábrica.
 * A ideia aqui é guardar tudo num banco relacional (tabela 'event') e usar o
 * campo 'eventId' como a chave pra evitar que a gente processe o mesmo evento duas vezes.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
@Entity
@Table(name = "event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    /**
     * ID interno do banco de dados. Usei UUID pra fugir de problemas
     * se o sistema precisar escalar ou rodar em clusters.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    /**
     * O ID que vem do equipamento (tipo EVT-1001).
     * É o cara responsável por garantir a idempotência. Coloquei unique=true
     * pra própria base de dados segurar a bronca se tentarem mandar duplicado.
     */
    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    /**
     * O tipo do evento: PRODUCTION ou QUALITY.
     * Isso vai ditar o que o sistema faz depois (só salva ou salva e audita).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    /**
     * Um texto livre explicando o que rolou no evento.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Quando o evento aconteceu lá na fábrica.
     */
    @Column(name = "event_time", nullable = false)
    private OffsetDateTime eventTime;

    /**
     * Quando o nosso sistema recebeu e salvou o evento.
     * Coloquei updatable=false só pra garantir que ninguém altere isso no futuro.
     */
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;}