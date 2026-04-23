package rdf.mapping.functions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashIri {

    public static String hashIri(String prefix, String... elements) {
        if (prefix == null || elements == null) {
            return null;
        }

        StringBuilder toHash = new StringBuilder(prefix);
        for (int i = 0; i < elements.length; i++) {
            toHash.append("|").append(elements[i]);
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(toHash.toString().getBytes(StandardCharsets.UTF_8));
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