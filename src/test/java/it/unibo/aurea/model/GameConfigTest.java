package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.GameConfig;
import it.unibo.aurea.model.api.Difficulty;

/**
 * this class tests that the configuration has sense.
 */
class GameConfigTest {
    private static final int STANDARD_CARDS_PER_SEMESTER = 6;
    private static final int STANDARD_SEMESTERS_PER_GAME = 6;
    private static final int MINIMAL_NUMBER_CARDS = 2;
    private static final int MINIMAL_NUMBER_SEMESTER = 2;

    @Test
    void testConfigurationValidity() {
        final GameConfig config = GameConfigFactory.createStandard(Difficulty.EASY);
        assertTrue(config.getCardsPerSemester() > 0, "Cards per semester must be strictly positive");
        assertTrue(config.getSemestersPerGame() > 0, "Semesters per game must be strictly positive");
    }

    @Test
    void testStandardConfigurationValidity() {
        final GameConfig config = GameConfigFactory.createStandard(Difficulty.EASY);
        assertTrue(config.getCardsPerSemester() == STANDARD_CARDS_PER_SEMESTER,
            "Cards per semester should be " + STANDARD_CARDS_PER_SEMESTER);
        assertTrue(config.getSemestersPerGame() == STANDARD_SEMESTERS_PER_GAME,
            "Semesters per game should be " + STANDARD_SEMESTERS_PER_GAME);
    }

    @Test
    void testShortConfigurationValidity() {
        final GameConfig config = GameConfigFactory.createShort(Difficulty.EASY);
        assertTrue(config.getCardsPerSemester() == MINIMAL_NUMBER_CARDS,
            "Cards per semester should be " + MINIMAL_NUMBER_CARDS);
        assertTrue(config.getSemestersPerGame() == MINIMAL_NUMBER_SEMESTER,
            "Semesters per game should be " + MINIMAL_NUMBER_SEMESTER);
    }
}
