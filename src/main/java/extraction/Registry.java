package extraction;

import java.nio.file.Path;
import java.util.List;

public class Registry {
    private final Path dataDir;
    private List<Hook> hooks;

    public Registry(Path dataDir) {
        this.dataDir = dataDir;
    }

    public void register(Hook... hooks) {
        this.hooks = List.of(hooks);
    }

    public ProcessingContext run(){
        ProcessingContext context = new ProcessingContext();
        for (Hook hook : hooks) {
            hook.setDataDir(dataDir);
            System.out.println("[Registry] Running hook: " + hook.getName());
            hook.execute(context);
        }
        return context;
    }
}