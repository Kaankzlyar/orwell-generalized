package rdf.mapping;
import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.Executor;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.RDF4JStore;
import be.ugent.rml.store.QuadStore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.rio.RDFFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RDFMapper {
    
    private List<Path> mappingFiles;
    private boolean reconciliationEnabled = true;
    private static final Path OUTPUT_PATH = Path.of("output", "graph.ttl");
    private static final Path GENERATED_MAPPINGS_BASE_PATH = Path.of("tmp", "mappings");
    private static final RDFFormat OUTPUT_FORMAT = RDFFormat.TURTLE;
    private static final String WIKIDATA_PREFIX = "wd";
    private static final String WIKIDATA_NAMESPACE = "http://www.wikidata.org/entity/";

    public Path map() throws IOException, InterruptedException {
        if (mappingFiles == null || OUTPUT_FORMAT == null) {
            throw new IllegalStateException("Mapping files and output path must be set before running the mapper.");
        }

        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files provided to RDFMapper.");
        }

        if (OUTPUT_PATH.getParent() != null) {
            Files.createDirectories(OUTPUT_PATH.getParent());
        }

        String cwd = System.getProperty("user.dir");
        String mappingParent = GENERATED_MAPPINGS_BASE_PATH.toAbsolutePath().normalize().toString();

        try {
            QuadStore rmlStore = new RDF4JStore();
            for (Path mappingFile : mappingFiles) {
                try (InputStream mappingStream = Files.newInputStream(mappingFile)) {
                    rmlStore.read(mappingStream, null, RDFFormat.TURTLE);
                }
            }

            RecordsFactory recordsFactory = new RecordsFactory(cwd, mappingParent);
            QuadStore outputStore = new RDF4JStore();
            Agent functionAgent = createFunctionAgent();

            try {
                Executor executor = new Executor(rmlStore, recordsFactory, outputStore, mappingParent, functionAgent);
                executor.verifySources(cwd, mappingParent);
                executor.execute(new ArrayList<>());
            } finally {
                functionAgent.close();
            }

            outputStore.copyNameSpaces(rmlStore);
            outputStore.addNameSpace(WIKIDATA_PREFIX, WIKIDATA_NAMESPACE);

            try (OutputStream out = Files.newOutputStream(OUTPUT_PATH)) {
                outputStore.write(out, OUTPUT_FORMAT.getName().toLowerCase());
            }
        } catch (Exception e) {
            throw new IOException("RMLMapper execution failed: " + e.getMessage(), e);
        }

        System.out.println("RMLMapper finished successfully. Output graph: " + OUTPUT_PATH);

        return OUTPUT_PATH;
    }

    private Agent createFunctionAgent() throws Exception {
        if (reconciliationEnabled) {
            return AgentFactory.createFromFnO(
                    "fno/functions_idlab.ttl",
                    "fno/functions_idlab_classes_java_mapping.ttl",
                    "functions/reconciliate.ttl"
            );
        }

        return AgentFactory.createFromFnO(
                "fno/functions_idlab.ttl",
                "fno/functions_idlab_classes_java_mapping.ttl"
        );
    }

}
