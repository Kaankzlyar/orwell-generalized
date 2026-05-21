package rdf.mapping;

import static org.junit.jupiter.api.Assertions.*;

import config.Config;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class MappingRunnerTest {

    @Test
    void outputPathForGroupUsesConfiguredGraphFileName() {
        String ext = Config.OUTPUT_FORMAT.getDefaultFileExtension();
        assertEquals(
            Path.of(Config.OUTPUT_DIR.toString(), "graph-xv." + ext),
            MappingRunner.outputPathForGroup("xv")
        );
    }

    @Test
    void sequentialMappingRunsEachGroup() {
        Map<String, List<Path>> groups = mappingGroups("xv", "xvi");
        List<Path> outputPaths = new ArrayList<>();

        MappingRunner.dispatch(
            groups,
            false,
            (mappingFiles, outputPath) -> () -> outputPaths.add(outputPath)
        );

        assertEquals(2, outputPaths.size());
        assertTrue(outputPaths.contains(MappingRunner.outputPathForGroup("xv")));
        assertTrue(outputPaths.contains(MappingRunner.outputPathForGroup("xvi")));
    }

    @Test
    void parallelMappingRunsGroupsConcurrently() throws InterruptedException {
        Map<String, List<Path>> groups = mappingGroups("xv", "xvi");
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch bothStarted = new CountDownLatch(1);

        MappingRunner.dispatch(
            groups,
            true,
            (mappingFiles, outputPath) -> () -> {
                started.countDown();
                assertTrue(started.await(2, TimeUnit.SECONDS),
                    "Both tasks should start concurrently");
                bothStarted.countDown();
            }
        );

        assertEquals(0, bothStarted.getCount());
    }

    @Test
    void parallelMappingPropagatesFailures() {
        Map<String, List<Path>> groups = mappingGroups("xv", "xvi");
        RuntimeException failure = new RuntimeException("mapping failed");

        RuntimeException actual = assertThrows(
            RuntimeException.class,
            () -> MappingRunner.dispatch(
                groups,
                true,
                (mappingFiles, outputPath) -> () -> { throw failure; }
            )
        );

        assertSame(failure, actual.getCause().getCause());
    }

    @Test
    void parallelMappingWrapsInterruptedTaskAsRuntimeException() {
        Map<String, List<Path>> groups = mappingGroups("xv", "xvi");

        RuntimeException actual = assertThrows(
            RuntimeException.class,
            () -> MappingRunner.dispatch(
                groups,
                true,
                (mappingFiles, outputPath) -> () -> {
                    throw new InterruptedException("mapping interrupted");
                }
            )
        );

        assertInstanceOf(InterruptedException.class, actual.getCause().getCause());
    }

    private static Map<String, List<Path>> mappingGroups(String... groupNames) {
        Map<String, List<Path>> groups = new LinkedHashMap<>();
        for (String groupName : groupNames) {
            groups.put(groupName, List.of(Path.of(groupName + ".ttl")));
        }
        return groups;
    }
}
