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
        // Remove the last :
        String key = keyBuilder.substring(0, keyBuilder.length() - 1);

        //System.out.println(
        //    Registry.getLookupTable().getOrDefault(lookupTable, Map.of())
        //);

        String result = Registry.getLookupTable()
            .getOrDefault(lookupTable, Map.of())
            .getOrDefault(key, null);

        System.out.println("Got result: " + result + " for key: " + key);

        return result;
    }
}
