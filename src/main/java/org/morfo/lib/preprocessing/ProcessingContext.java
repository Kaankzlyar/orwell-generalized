package org.morfo.lib.preprocessing;

  import java.nio.file.Path;
  import java.util.HashMap;
  import java.util.Map;
  import java.util.Optional;
  import java.util.Set;

  public class ProcessingContext {
      private final Map<String, Map<String, String>> lookupTables = new HashMap<>();
      private final Path dataDir;
      private final Set<String> disabledPartitions;

      public ProcessingContext(Path dataDir, Set<String> disabledPartitions) {
          this.dataDir = dataDir;
          this.disabledPartitions = Set.copyOf(disabledPartitions);
      }

      public Path getDataDir() {
          return dataDir;
      }

      public Set<String> getDisabledPartitions() {
          return disabledPartitions;
      }

      public void registerLookupTable(String name, Map<String, String> table) {
          lookupTables.put(name, table);
      }

      public Map<String, Map<String, String>> getLookupTable() {
          return lookupTables;
      }

      public Optional<Map<String, String>> getLookupTable(String name) {
          return Optional.ofNullable(lookupTables.get(name));
      }
  }