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
                rml:source "dummy.xml" ;
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
                rml:source "dummy.xml" ;
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
                rml:source "dummy.xml" ;
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
        assertFalse(createdContent.contains("dummy.xml"));
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
                rml:source "dummy.xml" ;
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
                rml:source "dummy.xml" ;
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
}
