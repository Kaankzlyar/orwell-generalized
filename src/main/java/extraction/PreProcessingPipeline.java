package extraction;

import java.nio.file.Path;
import java.util.List;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PreProcessingPipeline {
    private List<PreProcessingHook> hooks;

    public void register(PreProcessingHook... hooks) {
        this.hooks = List.of(hooks);
    }

    public ProcessingContext run(Path xmlInput){
        ProcessingContext context = new ProcessingContext(xmlInput);
        for (PreProcessingHook hook : hooks) {
            System.out.println("[Pipeline] Running hook: " + hook.getName());
            hook.execute(context);
        }
        return context;
    }
}