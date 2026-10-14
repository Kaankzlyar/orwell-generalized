package core.preprocessing;

import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class HookCatalog {

    private final Map<String, Hook> hooksByName = new LinkedHashMap<>();

    public HookCatalog(Iterable<Hook> providers) {
        for (Hook hook : providers) {
            String name = hook.getName();
            Hook existing = hooksByName.putIfAbsent(name, hook);
            if (existing != null) {
                throw new IllegalStateException(
                      "Duplicate hook name '" + name + "': "
                          + describe(existing) + " and " + describe(hook)
                  );
            }
        }
    }

        public static HookCatalog fromServiceLoader (){
            return new HookCatalog(ServiceLoader.load(Hook.class));
        }

        public List<Hook> resolve(List<String> names, Path manifestFile) {
            List<Hook> resolvedHooks = new ArrayList<>();
            for (String name : names) {
                Hook hook = hooksByName.get(name);
                if (hook == null) {
                    throw new IllegalStateException(
                        "Unknown preprocessing hook '" + name + "' in manifest file " + manifestFile
                        + ". Available hooks: " + hooksByName.keySet()
                    );
                }
                resolvedHooks.add(hook);
            }
            return resolvedHooks;
        }

        private static String describe(Hook hook) {
            CodeSource source = hook.getClass().getProtectionDomain().getCodeSource();
            if (source == null) {
                return hook.getClass().getName() + " (unknown source)";
            }
            URL location = source == null ? null : source.getLocation();
            return hook.getClass().getName() + " (" + location + ")";
        }


    }
