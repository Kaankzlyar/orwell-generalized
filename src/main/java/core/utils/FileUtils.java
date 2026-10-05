package core.utils;

import java.nio.file.Files;
import java.nio.file.Path;

import static core.config.Config.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.List;


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
       * Moves each domain folder of the temporary data directory into the real
       * data directory, replacing that domain's previous data. Folders of other
       * domains (e.g. another use case's data) are left untouched.
       * @param realDataDir the target directory (e.g. the original value of Config.DATA_DIR)
       * @throws IOException if an I/O error occurs
       */
    public static void moveTmpDataToData(Path realDataDir) throws IOException {
        Path tmpDataDir = TMP_DIR.resolve("data");
        if (!Files.exists(tmpDataDir)) return;

        Files.createDirectories(realDataDir);
        List<Path> domainDirs;
        try (var paths = Files.list(tmpDataDir)) {
            domainDirs = paths.filter(Files::isDirectory).toList();
        }
        for (Path domainDir : domainDirs) {
            Path target = realDataDir.resolve(domainDir.getFileName());
            if (Files.exists(target))
                deleteDirectory(target);
            Files.move(domainDir, target);
        }
    }
}
