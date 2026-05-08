package cli;

public record Options(
    boolean reconciliationEnabled,
    boolean extractionEnabled,
    boolean mappingEnabled,
    boolean shaclEnabled,
    boolean throwOnShaclUnconform,
    boolean printShaclReport,
    boolean logEnabled
) {
    public static Options defaults() {
        return new Builder().build();
    }

    public static final class Builder {
        boolean reconciliationEnabled = true;
        boolean extractionEnabled = true;
        boolean mappingEnabled = true;
        boolean shaclEnabled = true;
        boolean throwOnShaclUnconform = true;
        boolean printShaclReport = true;
        boolean logEnabled = false;

        Builder() {}

        public Options build() {
            return new Options(
                reconciliationEnabled, extractionEnabled, mappingEnabled,
                shaclEnabled, throwOnShaclUnconform, printShaclReport, logEnabled
            );
        }
    }
}
