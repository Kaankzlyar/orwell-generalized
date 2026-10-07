package core.rdf.mapping.functions;

import org.apache.commons.text.StringEscapeUtils;

public class StripHtml {

    public static String stripHtml(String value) {
        if (value == null) {
            return null;
        }
        String strippedValue = value.replaceAll("<[^>]*>", "");
        String result = StringEscapeUtils.unescapeHtml4(strippedValue)
                .replaceAll("(?U)^\\s+|\\s+$", "");
        return result.isEmpty() ? null : result;
    }
}
