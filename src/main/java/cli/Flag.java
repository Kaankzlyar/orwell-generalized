package cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public enum Flag {
    DISABLE_RECONCILIATION(
        "dr", "disable-reconciliation",
        "Disable reconciliation with Wikidata",
        b -> b.reconciliationEnabled = false
    ),
    DISABLE_EXTRACTION(
        "de", "disable-extraction",
        "Disable the data extraction phase",
        b -> b.extractionEnabled = false
    ),
    DISABLE_MAPPING(
        "dm", "disable-mapping",
        "Disable the RDF mapping phase",
        b -> b.mappingEnabled = false
    ),
    DISABLE_SHACL(
        "ds", "disable-shacl",
        "Disable SHACL validation entirely",
        b -> b.shaclEnabled = false
    ),
    DISABLE_SHACL_FAILURE(
        "df", "disable-shacl-failure",
        "Do not throw on SHACL violation",
        b -> b.throwOnShaclUnconform = false
    ),
    DISABLE_SHACL_REPORT(
        "r", "disable-shacl-report",
        "Do not print the SHACL validation report",
        b -> b.printShaclReport = false
    ),
    ENABLE_LOG(
        "l", "enable-log",
        "Enable reconciliation request logging",
        b -> b.logEnabled = true
    ),
    HELP(
        "h", "help",
        "Show this help message and exit",
        b -> {}
    );

    private static final Map<String, Flag> BY_LONG = new HashMap<>();
    private static final Map<String, Flag> BY_SHORT = new HashMap<>();

    static {
        for (Flag flag : values()) {
            BY_LONG.put("--" + flag.longName, flag);
            BY_SHORT.put("-" + flag.shortName, flag);
        }
    }

    private final String shortName;
    private final String longName;
    private final String description;
    private final Consumer<Options.Builder> action;

    public String shortName() { return shortName; }
    public String longName() { return longName; }
    public String description() { return description; }

    Flag(String shortName, String longName, String description, Consumer<Options.Builder> action) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
        this.action = action;
    }

    public static Optional<Flag> fromArg(String arg) {
        Flag flag = BY_LONG.get(arg);
        if (flag == null) flag = BY_SHORT.get(arg);
        return Optional.ofNullable(flag);
    }

    public static String usage() {
        var sb = new StringBuilder("Usage: orwell [options]\n\nOptions:\n");
        for (Flag f : values()) {
            if (f == HELP) continue;
            sb.append(String.format("  -%s, --%-28s %s%n", f.shortName, f.longName, f.description));
        }
        sb.append(String.format("  -%s, --%-28s %s%n", HELP.shortName, HELP.longName, HELP.description));
        return sb.toString();
    }

    public void applyTo(Options.Builder builder) {
        action.accept(builder);
    }
}
