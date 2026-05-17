package utils;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LegislatureUtilsTest {

    private final Map<String, String> legislatureTable = Map.of(
        "xvi", "2024-03-26|2025-06-02",
        "xvii", "2025-06-03|"
    );

    @Test
    void dateInFirstLegislature() {
        assertEquals("xvi", LegislatureUtils.findLegislatureByDate(legislatureTable, "2024-05-01"));
    }

    @Test
    void dateOnStartBoundary() {
        assertEquals("xvi", LegislatureUtils.findLegislatureByDate(legislatureTable, "2024-03-26"));
    }

    @Test
    void dateOnEndBoundary() {
        assertEquals("xvi", LegislatureUtils.findLegislatureByDate(legislatureTable, "2025-06-02"));
    }

    @Test
    void dateInCurrentLegislature() {
        assertEquals("xvii", LegislatureUtils.findLegislatureByDate(legislatureTable, "2025-07-01"));
    }

    @Test
    void dateInCurrentLegislatureFarFuture() {
        assertEquals("xvii", LegislatureUtils.findLegislatureByDate(legislatureTable, "2030-01-01"));
    }

    @Test
    void dateBeforeAnyLegislature() {
        assertNull(LegislatureUtils.findLegislatureByDate(legislatureTable, "2023-01-01"));
    }

    @Test
    void dateBetweenLegislatures() {
        assertNull(LegislatureUtils.findLegislatureByDate(legislatureTable, "2025-06-02T23:59:59"));
    }

    @Test
    void nullDateReturnsNull() {
        assertNull(LegislatureUtils.findLegislatureByDate(legislatureTable, null));
    }

    @Test
    void nullTableReturnsNull() {
        assertNull(LegislatureUtils.findLegislatureByDate(null, "2024-05-01"));
    }

    @Test
    void emptyTableReturnsNull() {
        assertNull(LegislatureUtils.findLegislatureByDate(Map.of(), "2024-05-01"));
    }

    @Test
    void singleEndedLegislature() {
        Map<String, String> single = Map.of("xv", "2022-03-29|2024-03-25");
        assertEquals("xv", LegislatureUtils.findLegislatureByDate(single, "2023-01-01"));
        assertNull(LegislatureUtils.findLegislatureByDate(single, "2024-06-01"));
    }

    @Test
    void singleCurrentLegislature() {
        Map<String, String> single = Map.of("xvii", "2025-06-03|");
        assertEquals("xvii", LegislatureUtils.findLegislatureByDate(single, "2025-06-03"));
        assertEquals("xvii", LegislatureUtils.findLegislatureByDate(single, "2026-05-17"));
    }
}
