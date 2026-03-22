package extraction;

import java.net.URI;
import java.util.List;

public sealed interface SourceNode permits SourceNode.SourceValue, SourceNode.SourceObject {

    String key();

    record SourceValue(String key, URI uri) implements SourceNode {}

    record SourceObject(String key, List<SourceNode> children) implements SourceNode {}
}
