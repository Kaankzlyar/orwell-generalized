package cli;

public class CliParser {

    public static Options parse(String[] args) {
        Options.Builder builder = new Options.Builder();

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

            flag.applyTo(builder);
            String label = flag.longName().replace('-', ' ');
            System.out.println(
                Character.toUpperCase(label.charAt(0)) + label.substring(1) + "."
            );
        }

        return builder.build();
    }
}
