package rdf.mapping;

import config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MappingPairPlannerTest {

    private static final String EXTRACTOR_NAME = "testextractor";
    private static final String MAPPING_NAME = "test";

    @TempDir
    Path tempDir;

    private Path originalDataDir;
    private Path originalMappingsDir;
    private Path originalTmpDir;

    @BeforeEach
    void setUp() {
        originalDataDir = Config.DATA_DIR;
        originalMappingsDir = Config.MAPPINGS_DIR;
        originalTmpDir = Config.TMP_DIR;
        
        Config.DATA_DIR = tempDir.resolve("data");
        Config.MAPPINGS_DIR = tempDir.resolve("mappings");
        Config.TMP_DIR = tempDir.resolve("tmp");
        
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
        
        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
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
        
        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);
        
        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME);
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve("data1.xml"), "<root><item><id>1</id></item></root>");
        
        MappingPairPlanner planner = new MappingPairPlanner();
        List<Path> result = planner.createMappingPairs();
        
        assertEquals(1, result.size());
        assertTrue(result.get(0).getFileName().toString().equals("data1.ttl"));
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
        
        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);
        
        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME);
        Files.createDirectories(dataDir);
        Path xmlFile = dataDir.resolve("data1.xml");
        Files.writeString(xmlFile, "<root><item><id>1</id></item></root>");
        
        MappingPairPlanner planner = new MappingPairPlanner();
        List<Path> result = planner.createMappingPairs();
        
        String createdContent = Files.readString(result.get(0));
        assertTrue(createdContent.contains(xmlFile.toAbsolutePath().normalize().toString().replace("\\", "/")));
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
        
        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);
        
        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME);
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve("data1.xml"), "<root><item><id>1</id></item></root>");
        
        MappingPairPlanner planner = new MappingPairPlanner();
        List<Path> result = planner.createMappingPairs();
        
        String createdContent = Files.readString(result.get(0));
        assertTrue(createdContent.contains("@base <http://example.org/mappings/" + EXTRACTOR_NAME + "/" + MAPPING_NAME + "/data1/>"));
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
        
        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);
        
        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME);
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve("a.xml"), "<root><item><id>a</id></item></root>");
        Files.writeString(dataDir.resolve("b.xml"), "<root><item><id>b</id></item></root>");
        Files.writeString(dataDir.resolve("c.xml"), "<root><item><id>c</id></item></root>");
        
        MappingPairPlanner planner = new MappingPairPlanner();
        List<Path> result = planner.createMappingPairs();
        
        assertEquals(3, result.size());
    }

    @Test
    void createMappingPairsUsesSourceNameWhenDifferentFromMappingName() throws Exception {
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

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve("petition.ttl");
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("otherdata");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve("data1.xml"), "<root><item><id>1</id></item></root>");

        MappingPairPlanner planner = new MappingPairPlanner();
        List<Path> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getFileName().toString().equals("data1.ttl"));
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

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dataDir = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("test");
        Files.createDirectories(dataDir);
        Files.writeString(dataDir.resolve("data1.xml"), "<root><items/><extras/></root>");

        MappingPairPlanner planner = new MappingPairPlanner();
        List<Path> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        String content = Files.readString(result.get(0));
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

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
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
        List<Path> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertEquals("data1.ttl", result.get(0).getFileName().toString());
        String content = Files.readString(result.get(0));
        assertTrue(content.contains(xmlA.toAbsolutePath().normalize().toString().replace("\\", "/")));
        assertTrue(content.contains(xmlB.toAbsolutePath().normalize().toString().replace("\\", "/")));
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

        Path mappingFile = Config.MAPPINGS_DIR.resolve(EXTRACTOR_NAME).resolve(MAPPING_NAME + ".ttl");
        Files.createDirectories(mappingFile.getParent());
        Files.writeString(mappingFile, mappingContent);

        Path dirA = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("sourceA");
        Path dirB = Config.DATA_DIR.resolve(EXTRACTOR_NAME).resolve("sourceB");
        Files.createDirectories(dirA);
        Files.createDirectories(dirB);
        Path xmlA = dirA.resolve("data1.xml");
        Path xmlB = dirB.resolve("data1.xml");
        Files.writeString(xmlA, "<root><a><id>A</id></a><a2><id>A2</id></a2></root>");
        Files.writeString(xmlB, "<root><b><id>B</id></b></root>");

        MappingPairPlanner planner = new MappingPairPlanner();
        List<Path> result = planner.createMappingPairs();

        assertEquals(1, result.size());
        assertEquals("data1.ttl", result.get(0).getFileName().toString());
        String content = Files.readString(result.get(0));
        assertTrue(content.contains(xmlA.toAbsolutePath().normalize().toString().replace("\\", "/")));
        assertTrue(content.contains(xmlB.toAbsolutePath().normalize().toString().replace("\\", "/")));
        assertFalse(content.contains("rml:source \"sourceA\" ;"));
        assertFalse(content.contains("rml:source \"sourceB\" ;"));
    }
}
