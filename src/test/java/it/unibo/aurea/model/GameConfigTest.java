package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.GameConfig;

/**
 * this class tests that the configuration has sense.
 */
class GameConfigTest {

    private GameConfig config;

    @BeforeEach
    void setUp() {
        config = GameConfigFactory.createStandard(it.unibo.aurea.model.api.Difficulty.EASY);
    }

    @Test
    void testStandardConfigurationValidity() {
        assertTrue(config.getCardsPerSemester() > 0, "Cards per semester must be strictly positive");
        assertTrue(config.getSemestersPerGame() > 0, "Semesters per game must be strictly positive");
    }
}
