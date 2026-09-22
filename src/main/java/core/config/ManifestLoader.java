package core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public final class ManifestLoader {
    
    @SuppressWarnings("unchecked")
    public static UseCaseManifest load(Path useCaseRoot) {
        Path manifestPath = useCaseRoot.resolve("dataset.yml");
        if (!Files.exists(manifestPath)) {
            throw new IllegalStateException("Manifest file does not exist: " + manifestPath);
        }

        Map <String, Object> root;
        try (InputStream inputStream = Files.newInputStream(manifestPath)) {
            root = new Yaml().load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read manifest file: " + manifestPath, e);
        }

        Map<String, Object> sourceMap = (Map<String, Object>) root.get("source");
         UseCaseManifest.SourceSpec source = new UseCaseManifest.SourceSpec(
              (String) sourceMap.get("kind"),
              (String) sourceMap.get("format"),
              (String) sourceMap.get("sourcesFile")
          );

        List<Map<String, Object>> partitionMaps = (List<Map<String, Object>>) root.get("partitions");
          List<UseCaseManifest.PartitionSpec> partitions = new ArrayList<>();
          for (Map<String, Object> p : partitionMaps) {
              partitions.add(new UseCaseManifest.PartitionSpec(
                  (String) p.get("code"),
                  (boolean) p.get("enabled")
              ));
          }

          List<String> preprocessing = (List<String>) root.get("preprocessing");

          Map<String, Object> reconciliationMap = (Map<String, Object>) root.get("reconciliation");
          UseCaseManifest.ReconciliationSpec reconciliation = new UseCaseManifest.ReconciliationSpec(
              (String) reconciliationMap.get("provider"),
              (boolean) reconciliationMap.get("enabled")
          );

          Map<String, Object> outputMap = (Map<String, Object>) root.get("output");
          UseCaseManifest.OutputSpec output = new UseCaseManifest.OutputSpec(
              (String) outputMap.get("namingTemplate"),
              (String) outputMap.get("format")
          );

          return new UseCaseManifest(
              (String) root.get("useCase"),
              (String) root.get("domain"),
              (String) root.get("partitionKey"),
              source,
              partitions,
              preprocessing,
              (String) root.get("mappingDir"),
              (String) root.get("functionsDir"),
              (String) root.get("ontologyDir"),
              (String) root.get("shaclDir"),
              reconciliation,
              output
          );
      }

      public static void applyTo(Path useCaseRoot, UseCaseManifest manifest) {
          Config.MAPPINGS_DIR = useCaseRoot.resolve(manifest.mappingDir());
          Config.SHACL_DIR = useCaseRoot.resolve(manifest.shaclDir());
          Config.FUNCTIONS_DIRS = List.of(
              Path.of("functions"),
              useCaseRoot.resolve(manifest.functionsDir())
          );

          Set<String> disabled = new HashSet<>();
          for (UseCaseManifest.PartitionSpec partition : manifest.partitions()) {
              if (!partition.enabled()) {
                  disabled.add(partition.code());
              }
          }
          Config.DISABLED_PARTITIONS = disabled;
      }
  }

