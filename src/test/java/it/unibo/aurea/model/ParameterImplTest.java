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

    private static final int SMALL_DELTA = 10;
    private static final int MEDIUM_DELTA = 15;
    private static final int LARGE_DELTA = 60;
    private static final int EXACT_DELTA = 50;
    private static final int TINY_DELTA = 5;

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
        parameter.modify(SMALL_DELTA);
        assertEquals(ParameterImpl.START_LEVEL + SMALL_DELTA, parameter.getLevel(),
            "Level should increase by delta");
    }

    @Test
    void testModifyDecrease() {
        parameter.modify(-MEDIUM_DELTA);
        assertEquals(ParameterImpl.START_LEVEL - MEDIUM_DELTA, parameter.getLevel(),
            "Level should decrease by delta");
    }

    @Test
    void testClampAtMax() {
        parameter.modify(LARGE_DELTA);
        assertEquals(ParameterImpl.MAX_LEVEL, parameter.getLevel(),
            "Level should be clamped at MAX_LEVEL");
    }

    @Test
    void testClampAtMin() {
        parameter.modify(-LARGE_DELTA);
        assertEquals(ParameterImpl.MIN_LEVEL, parameter.getLevel(),
            "Level should be clamped at MIN_LEVEL");
    }

    @Test
    void testDeadAtMax() {
        parameter.modify(LARGE_DELTA);
        assertFalse(parameter.isAlive(),
            "Parameter should be dead when level reaches MAX_LEVEL");
    }

    @Test
    void testDeadAtMin() {
        parameter.modify(-LARGE_DELTA);
        assertFalse(parameter.isAlive(),
            "Parameter should be dead when level reaches MIN_LEVEL");
    }

    @Test
    void testModifyIgnoredWhenDead() {
        parameter.modify(-LARGE_DELTA);
        final int levelAfterDeath = parameter.getLevel();
        parameter.modify(EXACT_DELTA);
        assertEquals(levelAfterDeath, parameter.getLevel(),
            "Modify should be ignored when parameter is already dead");
    }

    @Test
    void testDeathReasonAtMin() {
        parameter.modify(-LARGE_DELTA);
        final String reason = parameter.getDeathReason();
        assertTrue(reason.contains("dropped to zero"),
            "Death reason should mention 'dropped to zero'");
    }

    @Test
    void testDeathReasonAtMax() {
        parameter.modify(LARGE_DELTA);
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
        parameter.modify(SMALL_DELTA);
        assertEquals(ParameterImpl.START_LEVEL + SMALL_DELTA, notifiedLevel[0],
            "Observer should receive the updated level");
    }

    @Test
    void testObserverNotifiedWithCorrectType() {
        final ParameterType[] notifiedType = {null};
        parameter.addObserver((type, level) -> notifiedType[0] = type);
        parameter.modify(TINY_DELTA);
        assertEquals(ParameterType.FINANCES, notifiedType[0],
            "Observer should receive the correct parameter type");
    }

    @Test
    void testObserverNotNotifiedWhenDead() {
        parameter.modify(-LARGE_DELTA);
        final int[] callCount = {0};
        parameter.addObserver((type, level) -> callCount[0]++);
        parameter.modify(SMALL_DELTA);
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
        parameter.modify(EXACT_DELTA);
        assertEquals(ParameterImpl.MAX_LEVEL, parameter.getLevel(),
            "Level should be exactly MAX_LEVEL at boundary");
        assertFalse(parameter.isAlive(),
            "Parameter should be dead at exact MAX_LEVEL boundary");
    }

    @Test
    void testExactBoundaryMin() {
        parameter.modify(-EXACT_DELTA);
        assertEquals(ParameterImpl.MIN_LEVEL, parameter.getLevel(),
            "Level should be exactly MIN_LEVEL at boundary");
        assertFalse(parameter.isAlive(),
            "Parameter should be dead at exact MIN_LEVEL boundary");
    }
}
