package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.ParameterType;

/**
 * Unit tests for {@link ParameterImpl}.
 *
 * <p>Covers level clamping, alive/dead transitions, observer notification,
 * and fail-fast validation.
 */
class ParameterImplTest {

    private ParameterImpl parameter;

    @BeforeEach
    void setUp() {
        parameter = new ParameterImpl(ParameterType.FINANCES);
    }

    @Test
    void testInitialLevel() {
        assertEquals(ParameterImpl.START_LEVEL, parameter.getLevel(),
            "Parameter should start at START_LEVEL");
    }

    @Test
    void testInitiallyAlive() {
        assertTrue(parameter.isAlive(), "Parameter should be alive at start");
    }

    @Test
    void testModifyIncrease() {
        parameter.modify(10);
        assertEquals(ParameterImpl.START_LEVEL + 10, parameter.getLevel(),
            "Level should increase by delta");
    }

    @Test
    void testModifyDecrease() {
        parameter.modify(-15);
        assertEquals(ParameterImpl.START_LEVEL - 15, parameter.getLevel(),
            "Level should decrease by delta");
    }

    @Test
    void testClampAtMax() {
        parameter.modify(60);
        assertEquals(ParameterImpl.MAX_LEVEL, parameter.getLevel(),
            "Level should be clamped at MAX_LEVEL");
    }

    @Test
    void testClampAtMin() {
        parameter.modify(-60);
        assertEquals(ParameterImpl.MIN_LEVEL, parameter.getLevel(),
            "Level should be clamped at MIN_LEVEL");
    }

    @Test
    void testDeadAtMax() {
        parameter.modify(60);
        assertFalse(parameter.isAlive(),
            "Parameter should be dead when level reaches MAX_LEVEL");
    }

    @Test
    void testDeadAtMin() {
        parameter.modify(-60);
        assertFalse(parameter.isAlive(),
            "Parameter should be dead when level reaches MIN_LEVEL");
    }

    @Test
    void testModifyIgnoredWhenDead() {
        parameter.modify(-60);
        final int levelAfterDeath = parameter.getLevel();
        parameter.modify(50);
        assertEquals(levelAfterDeath, parameter.getLevel(),
            "Modify should be ignored when parameter is already dead");
    }

    @Test
    void testDeathReasonAtMin() {
        parameter.modify(-60);
        final String reason = parameter.getDeathReason();
        assertTrue(reason.contains("dropped to zero"),
            "Death reason should mention 'dropped to zero'");
    }

    @Test
    void testDeathReasonAtMax() {
        parameter.modify(60);
        final String reason = parameter.getDeathReason();
        assertTrue(reason.contains("maximum capacity"),
            "Death reason should mention 'maximum capacity'");
    }

    @Test
    void testDeathReasonWhenAlive() {
        final String reason = parameter.getDeathReason();
        assertEquals("Still alive!", reason,
            "Death reason should be 'Still alive!' when parameter is alive");
    }

    @Test
    void testAddNullObserverThrows() {
        assertThrows(NullPointerException.class,
            () -> parameter.addObserver(null),
            "Adding null observer should throw NullPointerException");
    }

    @Test
    void testObserverNotified() {
        final int[] notifiedLevel = {-1};
        parameter.addObserver((type, level) -> notifiedLevel[0] = level);
        parameter.modify(10);
        assertEquals(ParameterImpl.START_LEVEL + 10, notifiedLevel[0],
            "Observer should receive the updated level");
    }

    @Test
    void testObserverNotifiedWithCorrectType() {
        final it.unibo.aurea.model.api.ParameterType[] notifiedType =
            {null};
        parameter.addObserver((type, level) -> notifiedType[0] = type);
        parameter.modify(5);
        assertEquals(ParameterType.FINANCES, notifiedType[0],
            "Observer should receive the correct parameter type");
    }

    @Test
    void testObserverNotNotifiedWhenDead() {
        parameter.modify(-60);
        final int[] callCount = {0};
        parameter.addObserver((type, level) -> callCount[0]++);
        parameter.modify(10);
        assertEquals(0, callCount[0],
            "Observer should not be notified when parameter is dead");
    }

    @Test
    void testGetName() {
        assertEquals(ParameterType.FINANCES, parameter.getName(),
            "getName() should return the correct ParameterType");
    }

    @Test
    void testExactBoundaryMax() {
        parameter.modify(50);
        assertEquals(ParameterImpl.MAX_LEVEL, parameter.getLevel(),
            "Level should be exactly MAX_LEVEL at boundary");
        assertFalse(parameter.isAlive(),
            "Parameter should be dead at exact MAX_LEVEL boundary");
    }

    @Test
    void testExactBoundaryMin() {
        parameter.modify(-50);
        assertEquals(ParameterImpl.MIN_LEVEL, parameter.getLevel(),
            "Level should be exactly MIN_LEVEL at boundary");
        assertFalse(parameter.isAlive(),
            "Parameter should be dead at exact MIN_LEVEL boundary");
    }
}
