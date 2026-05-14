package extraction;

import java.net.URI;
import java.nio.file.Path;

public record DownloadTask(Path target, String key, URI uri) {}
