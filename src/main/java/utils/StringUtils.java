package utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern MARKS        = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALNUM    = Pattern.compile("[^\\p{Alnum}\\s]");
    private static final Pattern WHITESPACE   = Pattern.compile("\\s+");

    public static String normalize(String input) {
    if (input == null) return null;

    String decomposed = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
    return WHITESPACE.matcher(
               NON_ALNUM.matcher(
                   MARKS.matcher(decomposed).replaceAll("")
               ).replaceAll("")
           ).replaceAll("-");

    }

    public static String getLastWord(String input) {
        if (input == null || input.isEmpty()) return null;

        String normalizedInput = normalize(input);
        String[] words = normalizedInput.split("\\s+");
        return words[words.length - 1];
    }
}
