package com.certamen.artis.dto;

import com.certamen.artis.domain.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/**
 * DTO de entrada para a criação de um novo evento.
 * Usamos Records do Java 17 para deixar o código enxuto e imutável.
 * As anotações de validação garantem que o frontend não mande dados vazios.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
public record EventRequest(

        @NotBlank(message = "O eventId é obrigatório")
        @Schema(description = "ID único do evento gerado pelo equipamento", example = "EVT-1001")
        String eventId,

        @NotNull(message = "O eventType é obrigatório")
        @Schema(description = "Tipo do evento", example = "PRODUCTION")
        EventType eventType,

        @Schema(description = "Descrição do que aconteceu", example = "Linha de produção iniciada")
        String description,

        @NotNull(message = "O eventTime é obrigatório")
        @Schema(description = "Data e hora exata em que o evento ocorreu (padrão ISO-8601 com fuso)", example = "2026-05-20T10:00:00Z")
        OffsetDateTime eventTime
) {
}