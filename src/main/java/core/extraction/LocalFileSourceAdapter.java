package core.extraction;

import java.net.URI;
import java.nio.file.Path;

/**
 * Reads the same sources file layout as {@link HttpFileSourceAdapter}
 * (dataset -> partition -> location), but each location is a file path
 * relative to the working directory. Used for sources that cannot be
 * downloaded by a script, e.g. exports behind a browser challenge.
 */
public class LocalFileSourceAdapter extends HttpFileSourceAdapter {

    public LocalFileSourceAdapter(Path sourcesFile, String domain) {
        super(sourcesFile, domain);
    }

    @Override
    protected URI toUri(String location) {
        return Path.of(location).toAbsolutePath().normalize().toUri();
    }
}
