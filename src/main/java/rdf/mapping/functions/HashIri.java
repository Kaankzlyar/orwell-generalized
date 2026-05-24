package rdf.mapping.functions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashIri {

    public static String hashIri(String prefix, String... elements) {
        if (prefix == null || elements == null) {
            return null;
        }

        // The RMLMapper processor is not deterministic in the order of processing (i don't know why, but it is and i lost hours of my life debugging this so i know for a fact it's not deterministic), so we need to sort the elements to ensure that the same set of elements always produces the same hash, regardless of their order in the input.
        String[] sorted = elements.clone();
        Arrays.sort(sorted);

        StringBuilder toHash = new StringBuilder();
        for (String s : sorted) {
            toHash.append(s);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(
                toHash.toString().getBytes(StandardCharsets.UTF_8)
            );
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return prefix + hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
