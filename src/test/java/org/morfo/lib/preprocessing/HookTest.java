package org.morfo.lib.preprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HookTest {

    @TempDir
    Path tempDir;

    @Test
    void streamDocumentsSkipsDisabledPartitions() throws Exception {
        Path resourceDir = tempDir.resolve("ar").resolve("iniciativas");
        Files.createDirectories(resourceDir);
        Files.writeString(resourceDir.resolve("xvi.xml"), "<root/>");
        Files.writeString(resourceDir.resolve("xvii.xml"), "<root/>");

        ProcessingContext context = new ProcessingContext(tempDir, Set.of("xvi"));
        try (Stream<Path> documents = new TestHook().streamDocuments(context, "ar/iniciativas")) {
            List<String> names = documents.map(path -> path.getFileName().toString()).toList();
            assertEquals(List.of("xvii.xml"), names);
        }
    }

    private static class TestHook extends Hook {
        @Override
        public void execute(ProcessingContext context) {
        }

        @Override
        public String getName() {
            return "TestHook";
        }
    }
}