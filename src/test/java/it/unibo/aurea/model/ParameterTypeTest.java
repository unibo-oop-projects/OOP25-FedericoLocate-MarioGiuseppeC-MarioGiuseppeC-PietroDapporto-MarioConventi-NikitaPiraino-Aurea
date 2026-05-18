package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.ParameterType;

/**
 * Unit tests for {@link ParameterType}.
 *
 * <p>Verifies the Type-Safe Enum with Behaviour pattern: each constant
 * carries a human-readable display name accessible via getDisplayName(),
 * decoupling presentation logic from the enum identifier.
 */
class ParameterTypeTest {

    @Test
    void testAllValuesHaveDisplayName() {
        for (final ParameterType type : ParameterType.values()) {
            assertNotNull(type.getDisplayName(),
                type.name() + " should have a non-null display name");
        }
    }

    @Test
    void testDisplayNamesNotEmpty() {
        for (final ParameterType type : ParameterType.values()) {
            assertTrue(type.getDisplayName().length() > 0,
                type.name() + " should have a non-empty display name");
        }
    }

    @Test
    void testFinancesDisplayName() {
        assertEquals("Finances", ParameterType.FINANCES.getDisplayName());
    }

    @Test
    void testStudentsDisplayName() {
        assertEquals("Students", ParameterType.STUDENTS.getDisplayName());
    }

    @Test
    void testProfessorsDisplayName() {
        assertEquals("Professors", ParameterType.PROFESSORS.getDisplayName());
    }

    @Test
    void testReputationDisplayName() {
        assertEquals("Reputation", ParameterType.REPUTATION.getDisplayName());
    }

    @Test
    void testFourValuesExist() {
        assertEquals(4, ParameterType.values().length,
            "There should be exactly 4 parameter types");
    }

    @Test
    void testDisplayNameDiffersFromEnumName() {
        for (final ParameterType type : ParameterType.values()) {
            assertEquals(
                type.name().charAt(0),
                type.getDisplayName().toUpperCase(java.util.Locale.ROOT).charAt(0),
                type.name() + " display name should start with same letter as enum name"
            );
        }
    }
}
