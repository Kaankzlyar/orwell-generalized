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
import java.util.stream.Stream;

import org.eclipse.rdf4j.rio.RDFFormat;

import config.Config;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RDFMapper {
    
    private List<Path> mappingFiles;
    private static final String WIKIDATA_PREFIX = "wd";
    private static final String WIKIDATA_NAMESPACE = "http://www.wikidata.org/entity/";

    public void map() throws IOException, InterruptedException {
        if (mappingFiles.isEmpty()) {
            throw new IllegalStateException("No mapping files provided to RDFMapper.");
        }

        if (Config.OUTPUT_PATH.getParent() != null) {
            Files.createDirectories(Config.OUTPUT_PATH.getParent());
        }

        String cwd = System.getProperty("user.dir");
        String mappingParent = Config.GENERATED_MAPPINGS_BASE_DIR.toAbsolutePath().normalize().toString();

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

            try (OutputStream out = Files.newOutputStream(Config.OUTPUT_PATH)) {
                outputStore.write(out, Config.OUTPUT_FORMAT.getName().toLowerCase());
            }
        } catch (Exception e) {
            throw new IOException("RMLMapper execution failed: " + e.getMessage(), e);
        }

        System.out.println("RMLMapper finished successfully.\nOutput graph: " + Config.OUTPUT_PATH);
    }

    private Agent createFunctionAgent() throws Exception {
        List<String> functionFiles = new ArrayList<>();
        functionFiles.add("fno/functions_idlab.ttl");
        functionFiles.add("fno/functions_idlab_classes_java_mapping.ttl");
        functionFiles.add("functions_grel.ttl");
        functionFiles.add("grel_java_mapping.ttl");

        for (Path functionFile : getFunctionList()) {
            functionFiles.add(functionFile.toString().replace("\\", "/"));
        }
        
        return AgentFactory.createFromFnO(
            functionFiles.toArray(String[]::new)
        );
    }

    private List<Path> getFunctionList() {
        List<Path> list = new ArrayList<>();

        if (!Files.isDirectory(Config.FUNCTIONS_DIR)) {
            return list;
        }

        try (Stream<Path> stream = Files.list(Config.FUNCTIONS_DIR)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".ttl"))
                    .forEach(list::add);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read function files from " + Config.FUNCTIONS_DIR, e);
        }

        return list;
    }

}
