package rdf.mapping;

import be.ugent.idlab.knows.functions.agent.Agent;
import be.ugent.idlab.knows.functions.agent.AgentFactory;
import be.ugent.rml.Executor;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.RDF4JStore;
import config.Config;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.rdf4j.rio.RDFFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RDFMapper {

    private List<Path> mappingFiles;
    private Path outputPath;
    private static final String WIKIDATA_PREFIX = "wd";
    private static final String WIKIDATA_NAMESPACE =
        "http://www.wikidata.org/entity/";

    public void map() throws IOException, InterruptedException {
        if (mappingFiles == null || mappingFiles.isEmpty()) {
            throw new IllegalStateException(
                "[RDFMapper] No mapping files provided to RDFMapper."
            );
        }

        Path targetPath = outputPath != null ? outputPath : Config.OUTPUT_PATH;

        if (targetPath.getParent() != null) {
            Files.createDirectories(targetPath.getParent());
        }

        String mappingParent =
            Config.GENERATED_MAPPINGS_BASE_DIR.toAbsolutePath()
                .normalize()
                .toString();

        try {
            QuadStore rmlStore = new RDF4JStore();
            for (Path mappingFile : mappingFiles) {
                try (
                    InputStream mappingStream = Files.newInputStream(
                        mappingFile
                    )
                ) {
                    rmlStore.read(mappingStream, null, RDFFormat.TURTLE);
                }
            }

            RecordsFactory recordsFactory = new RecordsFactory(
                mappingParent,
                mappingParent
            );
            QuadStore outputStore = new RDF4JStore();
            Agent functionAgent = createFunctionAgent();

            try {
                Executor executor = new Executor(
                    rmlStore,
                    recordsFactory,
                    outputStore,
                    mappingParent,
                    functionAgent
                );
                executor.verifySources(mappingParent, mappingParent);
                executor.execute(new ArrayList<>());
            } finally {
                functionAgent.close();
            }

            outputStore.copyNameSpaces(rmlStore);
            outputStore.addNameSpace(WIKIDATA_PREFIX, WIKIDATA_NAMESPACE);

            try (OutputStream out = Files.newOutputStream(targetPath)) {
                outputStore.write(
                    out,
                    Config.OUTPUT_FORMAT.getName().toLowerCase()
                );
            }
        } catch (Exception e) {
            throw new IOException(
                "[RDFMapper] RMLMapper execution failed: " + e.getMessage(),
                e
            );
        }

        System.out.println(
            "[RDFMapper] RMLMapper finished successfully.\nOutput graph: " +
                targetPath
        );
    }

    /**
     * Creates an Agent from the FnO function definition files under the @Config.FUNCTION_DIR
     * @return
     * @throws Exception
     */
    private Agent createFunctionAgent() throws Exception {
        List<String> functionFiles = new ArrayList<>();
        functionFiles.add("fno/functions_idlab.ttl");
        functionFiles.add("fno/functions_idlab_classes_java_mapping.ttl");
        functionFiles.add("functions_grel.ttl");
        functionFiles.add("grel_java_mapping.ttl");

        for (Path functionFile : getFunctionList()) {
            functionFiles.add(functionFile.toString().replace("\\", "/"));
        }

        return AgentFactory.createFromFnO(functionFiles.toArray(String[]::new));
    }

    /**
     * Returns the path of each FnO function definition file under the @Config.FUNCTION_DIR
     * @return
     */
    private List<Path> getFunctionList() {
        List<Path> list = new ArrayList<>();

        if (!Files.isDirectory(Config.FUNCTIONS_DIR)) {
            return list;
        }

        try (Stream<Path> stream = Files.list(Config.FUNCTIONS_DIR)) {
            stream.forEach(list::add);
        } catch (IOException e) {
            throw new RuntimeException(
                "[RDFMapper] Failed to read function files from " +
                    Config.FUNCTIONS_DIR,
                e
            );
        }

        return list;
    }
}
