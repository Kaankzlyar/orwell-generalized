package config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;

public class ConfigParser {

    private static final Path CONFIG_PATH = Path.of("config.yml");

    public static Set<String> parseDisabledLegislatures() {
        if (!Files.exists(CONFIG_PATH)) {
            return Collections.emptySet();
        }

        try (InputStream in = new FileInputStream(CONFIG_PATH.toFile())) {
            Yaml yaml = new Yaml();
            Object raw = yaml.load(in);
            if (!(raw instanceof Map<?, ?> root)) {
                return Collections.emptySet();
            }

            Object legisRaw = root.get("legislatures");
            if (!(legisRaw instanceof List<?> legislatures)) {
                return Collections.emptySet();
            }

            Set<String> disabled = new HashSet<>();
            for (Object item : legislatures) {
                if (item instanceof Map<?, ?> entry) {
                    Object code = entry.get("code");
                    Object enabled = entry.get("enabled");
                    if (
                        code != null &&
                        "false".equalsIgnoreCase(String.valueOf(enabled))
                    ) {
                        disabled.add(String.valueOf(code));
                    }
                }
            }
            return disabled;
        } catch (Exception e) {
            System.err.println(
                "[ConfigParser] Failed to parse config.yml: " + e.getMessage()
            );
            return Collections.emptySet();
        }
    }
}
