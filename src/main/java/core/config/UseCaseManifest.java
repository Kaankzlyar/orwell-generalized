package core.config;

import java.util.List;

/**
 * Typed view of a use case's {@code dataset.yml}. This is the seam a new use
 * case fills in instead of editing pipeline code under {@code core}.
 */
public record UseCaseManifest(
    String useCase,
    String domain,
    String partitionKey,
    SourceSpec source,
    List<PartitionSpec> partitions,
    List<String> preprocessing,
    String mappingDir,
    String functionsDir,
    String ontologyDir,
    String shaclDir,
    ReconciliationSpec reconciliation,
    OutputSpec output
) {
    public record SourceSpec(String kind, String format, String sourcesFile) {}

    public record PartitionSpec(String code, boolean enabled) {}

    public record ReconciliationSpec(String provider, boolean enabled) {}

    public record OutputSpec(String namingTemplate, String format) {}
}
