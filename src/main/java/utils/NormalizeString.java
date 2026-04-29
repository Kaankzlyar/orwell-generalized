package utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class NormalizeString {

    public static final Pattern MARKS        = Pattern.compile("\\p{M}+");
    public static final Pattern NON_ALNUM    = Pattern.compile("[^\\p{Alnum}\\s]");
    public static final Pattern WHITESPACE   = Pattern.compile("\\s+");

    public static String normalize(String input) {
    if (input == null) return null;

    String decomposed = Normalizer.normalize(input.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
    return WHITESPACE.matcher(
               NON_ALNUM.matcher(
                   MARKS.matcher(decomposed).replaceAll("")
               ).replaceAll("")
           ).replaceAll("-");

    }
}
