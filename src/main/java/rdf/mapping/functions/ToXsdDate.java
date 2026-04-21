package rdf.mapping.functions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class ToXsdDate {

    private static final DateTimeFormatter INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static String toXsdDate(String inputDate) {
        if (inputDate == null) {
            return null;
        }

        String normalized = inputDate.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        try {
            LocalDate parsed = LocalDate.parse(normalized, INPUT_FORMATTER);
            return parsed.toString();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}