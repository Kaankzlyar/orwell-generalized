package rdf.mapping.functions;

import java.util.Map;
import preprocessing.Registry;
import utils.StringUtils;

public class Lookup {

    public static String lookup(String lookupTable, String... lookupValues) {
        // Loop though lookup values and build the key by normalizing them and concatenating with a :
        StringBuilder keyBuilder = new StringBuilder();
        for (String value : lookupValues) {
            if (value == null) continue;

            keyBuilder.append(StringUtils.normalize(value)).append(":");
        }

        if (keyBuilder.length() == 0) return null;

        // Remove the last :
        String key = keyBuilder.substring(0, keyBuilder.length() - 1);

        String result = Registry.getLookupTable()
            .getOrDefault(lookupTable, Map.of())
            .getOrDefault(key, null);

        return result;
    }
}
