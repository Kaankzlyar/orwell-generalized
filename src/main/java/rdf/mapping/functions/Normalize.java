package rdf.mapping.functions;

import java.text.Normalizer;
import java.util.Locale;

public class Normalize {

    public static String normalize(String input) {
        if (input == null) {
            return null;
        }

        String trimmed = input.trim();
        String lowercase = trimmed.toLowerCase(Locale.ROOT);
        String decomposed = Normalizer.normalize(lowercase, Normalizer.Form.NFD);

        return decomposed.replaceAll("\\p{M}+", "").replaceAll(",", "-").replaceAll("\\s+", "-").replaceAll("\"", "").replaceAll("/", "-");
    }
}
