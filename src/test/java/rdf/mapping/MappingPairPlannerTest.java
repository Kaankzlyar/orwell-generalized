package rdf.mapping;

import static org.junit.jupiter.api.Assertions.*;

import config.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MappingPairPlannerTest {

    private static final String EXTRACTOR_NAME = "testextractor";
    private static final String MAPPING_NAME = "test";

    @TempDir
    Path tempDir;

    private Path originalDataDir;
    private Path originalMappingsDir;
    private Path originalTmpDir;
    private Set<String> originalDisabledLegislatures;

    @BeforeEach
    void setUp() {
        originalDataDir = Config.DATA_DIR;
        originalMappingsDir = Config.MAPPINGS_DIR;
        originalTmpDir = Config.TMP_DIR;
        originalDisabledLegislatures = Config.DISABLED_LEGISLATURES;

        Config.DATA_DIR = tempDir.resolve("data");
        Config.MAPPINGS_DIR = tempDir.resolve("mappings");
        Config.TMP_DIR = tempDir.resolve("tmp");
        Config.DISABLED_LEGISLATURES = Collections.emptySet();

        try {
            Files.createDirectories(Config.DATA_DIR);
            Files.createDirectories(Config.MAPPINGS_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        Config.DATA_DIR = originalDataDir;
        Config.MAPPINGS_DIR = originalMappingsDir;
        Config.TMP_DIR = originalTmpDir;
        Config.DISABLED_LEGISLATURES = originalDisabledLegislatures;
    }

    @Test
    void createMappingPairsThrowsWhenDataDirMissing() throws Exception {
        Files.delete(Config.DATA_DIR);

        MappingPairPlanner planner = new MappingPairPlanner();

        assertThrows(IllegalStateException.class, planner::createMappingPairs);
    }

    @Test
    void createMappingPairsThrowsWhenMappingsDirMissing() throws Exception {
        Files.delete(Config.MAPPINGS_DIR);

        MappingPairPlanner planner = new MappingPairPlanner();

        assertThrows(IllegalStateException.class, planner::createMappingPairs);
    }

    @Test
    void createMappingPairsThrowsWhenNoExtractorDirs() {
        MappingPairPlanner planner = new MappingPairPlanner();

        assertThrows(IllegalStateException.class, planner::createMappingPairs);
    }

    @Test
    void createMappingPairsThrowsWhenNoMappingFiles() throws Exception {
        Files.createDirectories(Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME));

        MappingPairPlanner planner = new MappingPairPlanner();

        assertThrows(IllegalStateException.class, planner::createMappingPairs);
    }

    @Test
    void createMappingPairsThrowsWhenAllMappingsSkipped() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + ".ttl"
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        MappingPairPlanner planner = new MappingPairPlanner();

        assertThrows(IllegalStateException.class, planner::createMappingPairs);
    }

    @Test
    void createMappingPairsCreatesMappingForDataFile() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .
            @base <http://example.org/test/> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME
        );
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("data1.xml"),
            "<root><item><id>1</id></item></root>"
        );

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertTrue(result.containsKey("data1"));
        assertEquals(1, result.get("data1").size());
        assertEquals(
            "data1" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension(),
            result.get("data1").get(0).getFileName().toString()
        );
    }

    @Test
    void createMappingPairsReplacesSourcePath() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .
            @base <http://example.org/test/> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME
        );
        Files.createDirectories(dataDir);
        Path xmlFile = dataDir.resolve("data1.xml");
        Files.writeString(xmlFile, "<root><item><id>1</id></item></root>");

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        Path createdPath = result.get("data1").get(0);
        String createdContent = Files.readString(createdPath);
        assertTrue(
            createdContent.contains(
                xmlFile
                    .toAbsolutePath()
                    .normalize()
                    .toString()
                    .replace("\\", "/")
            )
        );
        assertFalse(createdContent.contains("rml:source \"test\" ;"));
    }

    @Test
    void createMappingPairsAppliesUniqueBase() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .
            @base <http://example.org/old/> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME
        );
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("data1.xml"),
            "<root><item><id>1</id></item></root>"
        );

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        String createdContent = Files.readString(result.get("data1").get(0));
        assertTrue(
            createdContent.contains(
                "@base <http://example.org/mappings/" +
                    EXTRACTOR_NAME +
                    "/" +
                    MAPPING_NAME +
                    "/data1/>"
            )
        );
        assertFalse(createdContent.contains("http://example.org/old/"));
    }

    @Test
    void createMappingPairsHandlesMultipleXmlFiles() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .
            @base <http://example.org/test/> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME
        );
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("a.xml"),
            "<root><item><id>a</id></item></root>"
        );
        Files.writeString(
            dataDir.resolve("b.xml"),
            "<root><item><id>b</id></item></root>"
        );
        Files.writeString(
            dataDir.resolve("c.xml"),
            "<root><item><id>c</id></item></root>"
        );

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        assertEquals(3, result.size());
        assertTrue(result.containsKey("a"));
        assertTrue(result.containsKey("b"));
        assertTrue(result.containsKey("c"));
    }

    @Test
    void createMappingPairsPreservesMappingSubdirectories() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME)
            .resolve("nested")
            .resolve(
                MAPPING_NAME +
                    "." +
                    Config.OUTPUT_FORMAT.getDefaultFileExtension()
            );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME
        );
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("data1.xml"),
            "<root><item><id>1</id></item></root>"
        );

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        Path expectedPath = Config.TMP_DIR.resolve(
            Config.MAPPINGS_DIR.getFileName()
        )
            .resolve(EXTRACTOR_NAME)
            .resolve("nested")
            .resolve(MAPPING_NAME)
            .resolve(
                "data1" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
            );
        assertEquals(expectedPath, result.get("data1").get(0));
    }

    @Test
    void createMappingPairsKeepsDuplicateMappingNamesInDifferentSubdirectories()
        throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path firstMapping = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME)
            .resolve("first")
            .resolve(
                MAPPING_NAME +
                    "." +
                    Config.OUTPUT_FORMAT.getDefaultFileExtension()
            );
        Path secondMapping = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME)
            .resolve("second")
            .resolve(
                MAPPING_NAME +
                    "." +
                    Config.OUTPUT_FORMAT.getDefaultFileExtension()
            );
        Files.createDirectories(firstMapping.getParent());
        Files.createDirectories(secondMapping.getParent());
        Files.writeString(firstMapping, mappingContent);
        Files.writeString(secondMapping, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME
        );
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("data1.xml"),
            "<root><item><id>1</id></item></root>"
        );

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        Path firstExpectedPath = Config.TMP_DIR.resolve(
            Config.MAPPINGS_DIR.getFileName()
        )
            .resolve(EXTRACTOR_NAME)
            .resolve("first")
            .resolve(MAPPING_NAME)
            .resolve(
                "data1" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
            );
        Path secondExpectedPath = Config.TMP_DIR.resolve(
            Config.MAPPINGS_DIR.getFileName()
        )
            .resolve(EXTRACTOR_NAME)
            .resolve("second")
            .resolve(MAPPING_NAME)
            .resolve(
                "data1" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
            );
        assertEquals(2, result.get("data1").size());
        assertTrue(result.get("data1").contains(firstExpectedPath));
        assertTrue(result.get("data1").contains(secondExpectedPath));
    }

    @Test
    void createMappingPairsSkipsDisabledLegislaturesWithoutRepeatingSkipLogs()
        throws Exception {
        Config.DISABLED_LEGISLATURES = Set.of("xv");
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .

            <#TestMap>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path firstMapping = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            "first" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Path secondMapping = Config.MAPPINGS_DIR.resolve(
            EXTRACTOR_NAME
        ).resolve(
            "second" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(firstMapping.getParent());
        Files.writeString(firstMapping, mappingContent);
        Files.writeString(secondMapping, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME
        );
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("xv.xml"),
            "<root><item><id>15</id></item></root>"
        );
        Files.writeString(
            dataDir.resolve("xvi.xml"),
            "<root><item><id>16</id></item></root>"
        );

        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setOut(
                new PrintStream(output, true, StandardCharsets.UTF_8)
            );

            MappingPairPlanner planner = new MappingPairPlanner();
            Map<String, List<Path>> result = planner.createMappingPairs();

            assertFalse(result.containsKey("xv"));
            assertTrue(result.containsKey("xvi"));
            assertEquals(2, result.get("xvi").size());
        } finally {
            System.setOut(originalOut);
        }

        String logs = output.toString(StandardCharsets.UTF_8);
        assertEquals(1, logs.split("Disabled legislatures:", -1).length - 1);
        assertFalse(logs.contains("Skipping disabled legislature"));
    }

    @Test
    void createMappingPairsUsesSourceNameWhenDifferentFromMappingName()
        throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .

            <#TestMap>
              rml:logicalSource [
                rml:source "otherdata" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/item"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Test>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            "petition" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(
            "otherdata"
        );
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("data1.xml"),
            "<root><item><id>1</id></item></root>"
        );

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertTrue(result.containsKey("data1"));
        assertEquals(
            "data1" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension(),
            result.get("data1").get(0).getFileName().toString()
        );
    }

    @Test
    void createMappingPairsWithMultipleBlocksSameSource() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .

            <#MapOne>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/items"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Item>
              ] .

            <#MapTwo>
              rml:logicalSource [
                rml:source "test" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/extras"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/Extra>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("test");
        Files.createDirectories(dataDir);
        Files.writeString(
            dataDir.resolve("data1.xml"),
            "<root><items/><extras/></root>"
        );

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertTrue(result.containsKey("data1"));
        String content = Files.readString(result.get("data1").get(0));
        assertTrue(content.contains("<#MapOne>"));
        assertTrue(content.contains("<#MapTwo>"));
    }

    @Test
    void createMappingPairsWithTwoSourcesDifferentDirs() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .

            <#MapOne>
              rml:logicalSource [
                rml:source "sourceA" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/a"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/A>
              ] .

            <#MapTwo>
              rml:logicalSource [
                rml:source "sourceB" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/b"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/B>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dirA = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("sourceA");
        Path dirB = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("sourceB");
        Files.createDirectories(dirA);
        Files.createDirectories(dirB);
        Path xmlA = dirA.resolve("data1.xml");
        Path xmlB = dirB.resolve("data1.xml");
        Files.writeString(xmlA, "<root><a><id>A</id></a></root>");
        Files.writeString(xmlB, "<root><b><id>B</id></b></root>");

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertTrue(result.containsKey("data1"));
        assertEquals(
            "data1" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension(),
            result.get("data1").get(0).getFileName().toString()
        );
        String content = Files.readString(result.get("data1").get(0));
        assertTrue(
            content.contains(
                xmlA.toAbsolutePath().normalize().toString().replace("\\", "/")
            )
        );
        assertTrue(
            content.contains(
                xmlB.toAbsolutePath().normalize().toString().replace("\\", "/")
            )
        );
        assertFalse(content.contains("rml:source \"sourceA\" ;"));
        assertFalse(content.contains("rml:source \"sourceB\" ;"));
    }

    @Test
    void createMappingPairsWithThreeBlocksTwoDirs() throws Exception {
        String mappingContent = """
            @prefix rr: <http://www.w3.org/ns/r2rml#> .
            @prefix rml: <http://semweb.mmlab.be/ns/rml#> .
            @prefix ql: <http://semweb.mmlab.be/ns/ql#> .

            <#MapOne>
              rml:logicalSource [
                rml:source "sourceA" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/a"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/A>
              ] .

            <#MapTwo>
              rml:logicalSource [
                rml:source "sourceA" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/a2"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/A2>
              ] .

            <#MapThree>
              rml:logicalSource [
                rml:source "sourceB" ;
                rml:referenceFormulation ql:XPath ;
                rml:iterator "/root/b"
              ] ;
              rr:subjectMap [
                rr:template "http://example.org/{id}" ;
                rr:class <http://example.org/B>
              ] .
            """;

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(
            MAPPING_NAME + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension()
        );
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dirA = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("sourceA");
        Path dirB = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("sourceB");
        Files.createDirectories(dirA);
        Files.createDirectories(dirB);
        Path xmlA = dirA.resolve("data1.xml");
        Path xmlB = dirB.resolve("data1.xml");
        Files.writeString(
            xmlA,
            "<root><a><id>A</id></a><a2><id>A2</id></a2></root>"
        );
        Files.writeString(xmlB, "<root><b><id>B</id></b></root>");

        MappingPairPlanner planner = new MappingPairPlanner();
        Map<String, List<Path>> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertTrue(result.containsKey("data1"));
        assertEquals(
            "data1" + "." + Config.OUTPUT_FORMAT.getDefaultFileExtension(),
            result.get("data1").get(0).getFileName().toString()
        );
        String content = Files.readString(result.get("data1").get(0));
        assertTrue(
            content.contains(
                xmlA.toAbsolutePath().normalize().toString().replace("\\", "/")
            )
        );
        assertTrue(
            content.contains(
                xmlB.toAbsolutePath().normalize().toString().replace("\\", "/")
            )
        );
        assertFalse(content.contains("rml:source \"sourceA\" ;"));
        assertFalse(content.contains("rml:source \"sourceB\" ;"));
    }
}
