package preprocessing;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Registry {
    private final Path dataDir;
    private List<Hook> hooks;
    private ProcessingContext context;

    public Registry(Path dataDir) {
        this.dataDir = dataDir;
    }

    public void register(Hook... hooks) {
        this.hooks = List.of(hooks);
    }

    public Map<String, Map<String, String>> getLookupTable() {
        return context.getLookupTable();
    }

    /**
     * Executes all registered hooks in the order they were registered. Each hook will have access to the shared ProcessingContext, incrementally building up the necessary data for the mapping and reconciliation process.
     */
    public void run(){
        context = new ProcessingContext();
        for (Hook hook : hooks) {
            hook.setDataDir(dataDir);
            System.out.println("[Registry] Running hook: " + hook.getName());
            hook.execute(context);
        }
    }
}