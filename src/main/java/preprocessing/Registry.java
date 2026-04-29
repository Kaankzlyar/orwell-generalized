package preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Registry {
    private static List<Hook> hooks = new ArrayList<>();
    private static ProcessingContext context;

    public static void reset() {
        hooks = new ArrayList<>();
        context = null;
    }

    public static void register(Hook... newHooks) {
        hooks = List.of(newHooks);
    }

    public static Map<String, Map<String, String>> getLookupTable() {
        return context.getLookupTable();
    }

    public static ProcessingContext getContext() {
        return context;
    }

    public static List<Hook> getHooks() {
        return hooks;
    }

    /**
     * Executes all registered hooks in the order they were registered. Each hook will have access to the shared ProcessingContext, incrementally building up the necessary data for the mapping and reconciliation process.
     */
    public static void run(){
        context = new ProcessingContext();
        for (Hook hook : hooks) {
            System.out.println("[Registry] Running hook: " + hook.getName());
            hook.execute(context);
        }
    }
}