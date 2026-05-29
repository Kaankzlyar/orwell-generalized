package cli;

public final class Options {

    private static boolean reconciliationEnabled = true;
    private static boolean extractionEnabled = true;
    private static boolean mappingEnabled = true;
    private static boolean shaclEnabled = true;
    private static boolean throwOnShaclUnconform = true;
    private static boolean printShaclReport = true;
    private static boolean logEnabled = false;
    private static boolean keepTmp = false;
    private static boolean fusekiEnabled = false;
    private static boolean parallelMappingEnabled = false;

    private Options() {}

    public static boolean reconciliationEnabled() {
        return reconciliationEnabled;
    }

    public static boolean extractionEnabled() {
        return extractionEnabled;
    }

    public static boolean mappingEnabled() {
        return mappingEnabled;
    }

    public static boolean shaclEnabled() {
        return shaclEnabled;
    }

    public static boolean throwOnShaclUnconform() {
        return throwOnShaclUnconform;
    }

    public static boolean printShaclReport() {
        return printShaclReport;
    }

    public static boolean logEnabled() {
        return logEnabled;
    }

    public static boolean keepTmp() {
        return keepTmp;
    }

    public static boolean fusekiEnabled() {
        return fusekiEnabled;
    }

    public static boolean parallelMappingEnabled() {
        return parallelMappingEnabled;
    }

    public static void parse(String[] args) {
        reset();

        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                System.out.print(Flag.usage());
                System.exit(0);
            }

            Flag flag = Flag.fromArg(arg).orElseThrow(() ->
                new IllegalArgumentException(
                    "Unknown flag: " + arg + "\n\n" + Flag.usage()
                )
            );

            switch (flag) {
                case DISABLE_RECONCILIATION -> reconciliationEnabled = false;
                case DISABLE_EXTRACTION -> extractionEnabled = false;
                case DISABLE_MAPPING -> mappingEnabled = false;
                case DISABLE_SHACL -> shaclEnabled = false;
                case DISABLE_SHACL_FAILURE -> throwOnShaclUnconform = false;
                case DISABLE_SHACL_REPORT -> printShaclReport = false;
                case ENABLE_LOG -> logEnabled = true;
                case KEEP_TMP -> keepTmp = true;
                case ENABLE_FUSEKI -> fusekiEnabled = true;
                case PARALLEL_MAPPING -> parallelMappingEnabled = true;
                case HELP -> {
                }
            }

            String label = flag.longName().replace('-', ' ');
            System.out.println(
                Character.toUpperCase(label.charAt(0)) +
                    label.substring(1) +
                    "."
            );
        }
    }

    private static void reset() {
        reconciliationEnabled = true;
        extractionEnabled = true;
        mappingEnabled = true;
        shaclEnabled = true;
        throwOnShaclUnconform = true;
        printShaclReport = true;
        logEnabled = false;
        keepTmp = false;
        fusekiEnabled = false;
        parallelMappingEnabled = false;
    }
}
