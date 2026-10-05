// Compares two RDF graphs for parity (e.g. main vs develop output).
// Usage (after .\gradlew.bat installDist):
//   $env:GRAPH_A="..\orwell-main\output\graph-xiv.ttl"; $env:GRAPH_B="output\graph-xiv.ttl"
//   jshell --class-path "build/install/orwell/lib/*" compare-graphs.jsh
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.*;

String pathA = System.getenv("GRAPH_A");
String pathB = System.getenv("GRAPH_B");
Model a = RDFDataMgr.loadModel(pathA);
Model b = RDFDataMgr.loadModel(pathB);
System.out.println("A: " + pathA + " -> " + a.size() + " triples");
System.out.println("B: " + pathB + " -> " + b.size() + " triples");
System.out.println("Isomorphic: " + a.isIsomorphicWith(b));

Model onlyA = a.difference(b);
Model onlyB = b.difference(a);
System.out.println("Only in A: " + onlyA.size() + " triples");
System.out.println("Only in B: " + onlyB.size() + " triples");

void sample(String label, Model m) {
    if (m.isEmpty()) return;
    System.out.println("--- sample " + label);
    StmtIterator it = m.listStatements();
    for (int i = 0; i < 15 && it.hasNext(); i++) System.out.println("  " + it.next());
}
sample("only in A", onlyA);
sample("only in B", onlyB);
/exit
