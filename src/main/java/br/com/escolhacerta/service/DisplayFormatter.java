package br.com.escolhacerta.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;

/** Formata somente a apresentação; os dados persistidos continuam em UTC/ISO. */
@Component("displayFormat")
public class DisplayFormatter {
    private static final Locale LOCALE = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", LOCALE);
    private final DateTimeFormatter dateTime;
    private final ObjectMapper json;

    public DisplayFormatter(ObjectMapper json, @Value("${app.display.time-zone:America/Sao_Paulo}") String zone) {
        this.json = json;
        this.dateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOCALE).withZone(ZoneId.of(zone));
    }

    public String dateTime(Instant value) {
        return value == null ? "" : dateTime.format(value);
    }

    public String answers(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            var answers = json.readValue(value, new TypeReference<LinkedHashMap<String, String>>() {});
            if (answers == null) return "";
            var result = new StringBuilder();
            answers.forEach((label, answer) -> {
                String display = answer == null ? "" : answer;
                if (display.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                    try { display = DATE.format(LocalDate.parse(display)); }
                    catch (java.time.format.DateTimeParseException ignored) { /* Mantém texto inválido. */ }
                }
                if (result.length() > 0) result.append("\n");
                result.append(label).append(": ").append(display);
            });
            return result.toString();
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            return value;
        }
    }
}
