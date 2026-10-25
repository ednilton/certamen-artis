package com.certamen.artis.dto;

import com.certamen.artis.config.json.FlexibleOffsetDateTimeDeserializer;
import com.certamen.artis.domain.EventType;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.time.OffsetDateTime;

/**
 * DTO de entrada para a criação de um novo evento.
 * Usamos Records do Java 17 para deixar o código enxuto e imutável.
 * As anotações de validação garantem que o frontend não mande dados vazios.
 *
 * O campo de data aceita tanto "eventTime" quanto "timestamp" (nome usado no payload de
 * exemplo do enunciado), com ou sem fuso. Sem fuso, assumimos UTC.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
public record EventRequest(

        @NotBlank(message = "O eventId é obrigatório")
        @Size(max = 255, message = "O eventId deve ter no máximo 255 caracteres")
        @Schema(description = "ID único do evento gerado pelo equipamento", example = "EVT-1001")
        String eventId,

        @NotNull(message = "O eventType é obrigatório")
        @Schema(description = "Tipo do evento", example = "PRODUCTION")
        EventType eventType,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        @Schema(description = "Descrição do que aconteceu", example = "Production line started")
        String description,

        @NotNull(message = "O eventTime é obrigatório")
        @JsonAlias("timestamp")
        @JsonDeserialize(using = FlexibleOffsetDateTimeDeserializer.class)
        @Schema(description = "Data e hora em que o evento ocorreu (ISO-8601). Também aceito como 'timestamp'. Sem fuso, é tratado como UTC.",
                example = "2026-05-20T10:00:00Z")
        OffsetDateTime eventTime
) {
}
