package core.rdf.mapping.functions;

import org.apache.commons.text.StringEscapeUtils;

public class StripHtml {
    
    public static String stripHtml(String value) {
        if (value == null) {
            return null;
        }
        String strippedValue = value.replaceAll("<[^>]*>", "");
        return StringEscapeUtils.unescapeHtml4(strippedValue);
    }
}
