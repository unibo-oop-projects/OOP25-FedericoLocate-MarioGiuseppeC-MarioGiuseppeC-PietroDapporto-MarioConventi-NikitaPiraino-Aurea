package it.unibo.aurea.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import it.unibo.aurea.model.api.Difficulty;

import it.unibo.aurea.controller.api.PlayerInfo;

/**
 * Unit tests for {@link PlayerInfo}.
 *
 * <p>Covers getter correctness, null validation (Fail-Fast),
 * and record-generated equals/toString behaviour.
 */
class PlayerInfoTest {

    private static final String RECTOR = "Federico Locatelli";
    private static final String FACULTY = "Engineering";

    @Test
    void testRectorName() {
        final PlayerInfo info = new PlayerInfo(RECTOR, FACULTY, Difficulty.NORMAL);
        assertEquals(RECTOR, info.rectorName(),
            "rectorName() should return the value passed at construction");
    }

    @Test
    void testFaculty() {
        final PlayerInfo info = new PlayerInfo(RECTOR, FACULTY, Difficulty.NORMAL);
        assertEquals(FACULTY, info.faculty(),
            "faculty() should return the value passed at construction");
    }

    @Test
    void testNullRectorNameThrows() {
        assertThrows(NullPointerException.class,
            () -> new PlayerInfo(null, FACULTY, Difficulty.NORMAL),
            "Null rectorName should throw NullPointerException");
    }

    @Test
    void testNullFacultyThrows() {
        assertThrows(NullPointerException.class,
            () -> new PlayerInfo(RECTOR, null, Difficulty.NORMAL),
            "Null faculty should throw NullPointerException");
    }

    @Test
    void testEquality() {
        final PlayerInfo infoA = new PlayerInfo(RECTOR, FACULTY, Difficulty.NORMAL);
final PlayerInfo infoB = new PlayerInfo(RECTOR, FACULTY, Difficulty.NORMAL);
        assertEquals(infoA, infoB,
            "Two PlayerInfo with same values should be equal");
    }

    @Test
    void testInequality() {
        final PlayerInfo infoA = new PlayerInfo(RECTOR, FACULTY, Difficulty.NORMAL);
        final PlayerInfo infoB = new PlayerInfo("Pietro Dapporto", FACULTY, Difficulty.NORMAL);
        assertNotEquals(infoA, infoB,
            "Two PlayerInfo with different rectorName should not be equal");
    }

    @Test
    void testToString() {
        final PlayerInfo info = new PlayerInfo(RECTOR, FACULTY, Difficulty.NORMAL);
        final String str = info.toString();
        assertTrue(str.contains(RECTOR),
            "toString() should contain rectorName");
        assertTrue(str.contains(FACULTY),
            "toString() should contain faculty");
    }
}
