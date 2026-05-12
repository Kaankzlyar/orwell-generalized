package query;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;

public class QueryRunner {

    public static void execute(Model model, Path queryFile) throws IOException {
        String queryString = Files.readString(queryFile);
        Query query = QueryFactory.create(queryString);
        try (var qexec = QueryExecutionFactory.create(query, model)) {
            var results = qexec.execSelect();
            ResultSetFormatter.out(System.out, results, query);
        }
    }

    public static void executeAll(Model model, Path queryDir)
        throws IOException {
        try (var files = Files.list(queryDir)) {
            files
                .filter(p -> p.toString().endsWith(".rq"))
                .sorted()
                .forEach(p -> {
                    try {
                        System.out.println(
                            "\n=== Query: " + p.getFileName() + " ==="
                        );
                        execute(model, p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
    }
}
