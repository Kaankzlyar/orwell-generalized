package utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;

import static config.Config.*;


public class FileUtils {

	/**
	 * Deletes the given directory and all its contents recursively.
	 * @param dir the directory to delete
	 * @throws IOException if an I/O error occurs
	 */
	private static void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try { Files.delete(path); }
                    catch (IOException e) { throw new UncheckedIOException(e); }
                });
        }
    }

    /**
     * Deletes the temporary directory and all its contents recursively.
     * @throws IOException if an I/O error occurs
     */
    public static void deleteTmpDir() throws IOException {
        deleteDirectory(TMP_DIR);
    }

    /**
     * Moves the temporary data directory to the real data directory atomically.
     * @param realDataDir the target directory (e.g. the original value of Config.DATA_DIR)
     * @throws IOException if an I/O error occurs
     */
    public static void moveTmpDataToData(Path realDataDir) throws IOException {
        Path tmpDataDir = TMP_DIR.resolve("data");
        if (!Files.exists(tmpDataDir)) return;

        Path stagingDir = TMP_DIR.resolve("data.staging");
        if (Files.exists(stagingDir))
            deleteDirectory(stagingDir);
        Files.move(tmpDataDir, stagingDir);

        if (Files.exists(realDataDir))
            deleteDirectory(realDataDir);
        Files.move(stagingDir, realDataDir);
    }
}
