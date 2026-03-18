package preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Registry {
    private List<Hook> hooks;
    private ProcessingContext context;

    public Registry() {
        this.hooks = new ArrayList<>();
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
            System.out.println("[Registry] Running hook: " + hook.getName());
            hook.execute(context);
        }
    }
}