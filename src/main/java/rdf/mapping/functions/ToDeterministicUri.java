package rdf.mapping.functions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Locale;

public class ToDeterministicUri {

    private static final String BASE_GRAPH_URI = "http://purl.org/polis/ar/graph#";

    public static String toDeterministicUri(
        String entityPrefix,
        String sourceSystem,
        String key1,
        String key2,
        String key3,
        String key4,
        String key5
    ) {
        return toDeterministicUri(entityPrefix, sourceSystem, new String[] { key1, key2, key3, key4, key5 });
    }

    public static String toDeterministicUri(
        String entityPrefix,
        String sourceSystem,
        String... keys
    ) {
        String prefix = sanitizePrefix(entityPrefix);
        StringBuilder canonical = new StringBuilder(normalize(sourceSystem));

        if (keys != null) {
            for (String key : keys) {
                String normalized = normalize(key);
                if (!normalized.isEmpty()) {
                    canonical.append('|').append(normalized);
                }
            }
        }

        String hash = sha256Hex(canonical.toString()).substring(0, 16);
        return BASE_GRAPH_URI + prefix + "_" + hash;
    }

    private static String sanitizePrefix(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "resource";
        }

        StringBuilder builder = new StringBuilder();
        for (char c : value.trim().toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '_') {
                builder.append(c);
            }
        }

        if (builder.length() == 0) {
            return "resource";
        }
        return builder.toString();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String collapsed = trimmed.replaceAll("\\s+", " ");
        String withoutAccents = Normalizer.normalize(collapsed, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");

        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}