package it.unibo.aurea.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.aurea.model.api.GameClock;
import it.unibo.aurea.model.api.GameConfig;

/**
 * this class is used to test the functionality of the gameClock
 * this is uìsed to chek that every modification to the implementation doesn't
 * contains errors in the functionality.
 */
final class GameClockTest {
    private GameClock clock;
    private GameConfig config;

    @BeforeEach
    void configuration() {
        config = GameConfigFactory.createStandard(it.unibo.aurea.model.api.Difficulty.EASY);
        clock = new GameClockImpl(config);
    }

    @Test
    void testSingleTurnProgression() {
        final int initialTurn = clock.getCurrentTurn();
        final int initialSemester = clock.getCurrentSemester();

        clock.nextTurn();

        // Currently, it assumes that after a single decision the semester does not change,
        // which is specific to the standard game mode.
        // In the future, a game mode could be introduced where a single card affects an entire parameter cycle.
        assertEquals(initialTurn + 1, clock.getCurrentTurn(), "The turn should advance by exactly 1");
        assertEquals(initialSemester, clock.getCurrentSemester(), "The semester cannot change after just one turn");
    }

    @Test
    void testSemesterFlow() {
        final int cardsPerSemester = config.getCardsPerSemester();

        for (int i = 0; i < cardsPerSemester; i++) {
            clock.nextTurn();
        }

        assertEquals(1, clock.getCurrentSemester(), "After playing all cards of a semester, the semester must increment");
        assertEquals(0, clock.getCurrentTurn(), "The turn counter must reset at the start of a new semester");
    }

    @Test
    void testEndofTime() {
        while (!clock.isTimeFinished()) {
            clock.nextTurn();
        }

        assertTrue(clock.isTimeFinished(), "The clock must report time is finished after all semesters are played");
    }
}
