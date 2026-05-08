package rdf;

import config.Config;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
}
