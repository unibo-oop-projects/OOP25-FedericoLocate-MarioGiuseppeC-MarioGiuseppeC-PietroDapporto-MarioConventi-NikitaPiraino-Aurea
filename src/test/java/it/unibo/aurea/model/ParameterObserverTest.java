package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.ParameterObserver;
import it.unibo.aurea.model.api.ParameterType;

/**
 * Unit tests for {@link ParameterObserver}.
 *
 * <p>Verifies that the interface is a true functional interface:
 * it can be instantiated as a lambda expression and its single
 * abstract method receives the correct arguments.
 */
class ParameterObserverTest {

    private static final int INITIAL_LEVEL = 50;
    private static final int UPDATED_LEVEL = 75;

    @Test
    void testIsInstantiableAsLambda() {
        // A @FunctionalInterface can be assigned from a lambda; if this
        // compiles and runs, the interface has exactly one abstract method (as we want).
        final ParameterObserver observer = (type, level) -> { };
        assertNotNull(observer, "ParameterObserver should be instantiable as a lambda");
    }

    @Test
    void testReceivesCorrectType() {
        final ParameterType[] receivedType = {null};
        final ParameterObserver observer = (type, level) -> receivedType[0] = type;
        observer.onParameterChanged(ParameterType.FINANCES, INITIAL_LEVEL);
        assertEquals(ParameterType.FINANCES, receivedType[0],
            "Observer should receive the correct ParameterType");
    }

    @Test
    void testReceivesCorrectLevel() {
        final int[] receivedLevel = {-1};
        final ParameterObserver observer = (type, level) -> receivedLevel[0] = level;
        observer.onParameterChanged(ParameterType.STUDENTS, UPDATED_LEVEL);
        assertEquals(UPDATED_LEVEL, receivedLevel[0],
            "Observer should receive the correct level value");
    }
}
