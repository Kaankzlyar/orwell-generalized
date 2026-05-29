package cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum Flag {
    DISABLE_RECONCILIATION(
        "dr",
        "disable-reconciliation",
        "Disable reconciliation with Wikidata"
    ),
    DISABLE_EXTRACTION(
        "de",
        "disable-extraction",
        "Disable the data extraction phase"
    ),
    DISABLE_MAPPING("dm", "disable-mapping", "Disable the RDF mapping phase"),
    DISABLE_SHACL("ds", "disable-shacl", "Disable SHACL validation"),
    DISABLE_SHACL_FAILURE(
        "df",
        "disable-shacl-failure",
        "Do not throw on SHACL violation"
    ),
    DISABLE_SHACL_REPORT(
        "r",
        "disable-shacl-report",
        "Do not print the SHACL validation report"
    ),
    ENABLE_LOG("l", "enable-log", "Enable reconciliation request logging"),
    KEEP_TMP("t", "keep-tmp", "Keep temporary files after processing"),
    ENABLE_FUSEKI("f", "enable-fuseki", "Push final graph to Fuseki"),
    PARALLEL_MAPPING(
        "p",
        "parallel-mapping",
        "Run RDF mapping groups in parallel"
    ),
    HELP("h", "help", "Show this help message and exit");

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

    public String shortName() {
        return shortName;
    }

    public String longName() {
        return longName;
    }

    public String description() {
        return description;
    }

    Flag(String shortName, String longName, String description) {
        this.shortName = shortName;
        this.longName = longName;
        this.description = description;
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
            sb.append(
                String.format(
                    "  -%s, --%-28s %s%n",
                    f.shortName,
                    f.longName,
                    f.description
                )
            );
        }
        sb.append(
            String.format(
                "  -%s, --%-28s %s%n",
                HELP.shortName,
                HELP.longName,
                HELP.description
            )
        );
        return sb.toString();
    }
}
