package com.certamen.artis.config.json;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

/**
 * Deserializador de data/hora mais tolerante.
 * Aceita data COM fuso ("2026-05-20T10:00:00Z" ou "2026-05-20T07:00:00-03:00")
 * e SEM fuso ("2026-05-20T10:00:00", que é o formato do exemplo do enunciado).
 * Quando vem sem fuso, a gente assume UTC. E sempre devolve em UTC,
 * pra API responder num formato único independente de quem mandou.
 *
 * @author Ednilton Rauh
 * @email edrauh@gmail.com
 */
public class FlexibleOffsetDateTimeDeserializer extends ValueDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext ctxt) {
        String text = parser.getValueAsString();
        if (text == null || text.isBlank()) {
            return null; // o @NotNull do DTO cuida do campo obrigatório
        }
        String value = text.trim();

        try {
            return OffsetDateTime.parse(value).withOffsetSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            // veio sem fuso, tenta como LocalDateTime logo abaixo
        }

        try {
            return LocalDateTime.parse(value).atOffset(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            throw ctxt.weirdStringException(value, OffsetDateTime.class,
                    "use o formato ISO-8601, ex.: 2026-05-20T10:00:00Z");
        }
    }
}
