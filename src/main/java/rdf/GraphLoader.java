package rdf;

import config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;

public class GraphLoader {

    public static Model loadGraph() {
        if (!Files.isDirectory(Config.OUTPUT_DIR)) {
            throw new IllegalStateException(
                "[GraphLoader] Output directory not found: " + Config.OUTPUT_DIR
            );
        }

        List<Path> graphPaths;
        try (var paths = Files.list(Config.OUTPUT_DIR)) {
            graphPaths = paths.toList();
        } catch (IOException e) {
            throw new RuntimeException(
                "[GraphLoader] Failed to list output directory: " +
                    Config.OUTPUT_DIR,
                e
            );
        }

        if (graphPaths.isEmpty()) {
            System.out.println(
                "[GraphLoader] No graph files found for loading. Returning early."
            );
            return null;
        }

        // Load the generated graphs into a single model
        Model finalGraph = ModelFactory.createDefaultModel();
        graphPaths.forEach(graphPath -> {
            System.out.println("[GraphLoader] Loading graph: " + graphPath);
            RDFDataMgr.read(finalGraph, graphPath.toString());
        });

        System.out.println(
            "[GraphLoader] Final graph size: " + finalGraph.size() + " triples"
        );

        return finalGraph;
    }

    public static void pushToFuseki(Model model) {
        String url = Config.FUSEKI_URL;
        System.out.println("\n[Fuseki] Pushing model to " + url + " ...");
        try (RDFConnection conn = RDFConnection.connect(url)) {
            conn.load(model);
            System.out.println(
                "[Fuseki] Done. SPARQL endpoint: " + url + "/sparql"
            );
        } catch (Exception e) {
            String msg = e.getMessage();
            System.err.println(
                "[Fuseki] ERROR: " +
                    (msg != null ? msg : e.getClass().getSimpleName())
            );
            System.err.println(
                "[Fuseki] Override the URL with: export ORWELL_FUSEKI_URL=http://your-server:PORT/ds"
            );
        }
    }
}
