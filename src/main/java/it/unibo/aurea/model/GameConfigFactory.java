package it.unibo.aurea.model;

import java.util.Objects;
import it.unibo.aurea.model.api.Difficulty;
import it.unibo.aurea.model.api.GameConfig;

/**
 * Factory dedicated to creating game configurations.
 * Centralizes initialization logic by hiding the concrete implementation class.
 */
public final class GameConfigFactory {
    private static final int STANDARD_CARDS_PER_SEMESTER = 6;
    private static final int STANDARD_SEMESTERS_PER_GAME = 6;
    private static final int MINIMAL_NUMBER_CARDS = 2;
    private static final int MINIMAL_NUMBER_SEMESTER = 2;

    private GameConfigFactory() { }

    /**
     * Creates a standard configuration (3 years, 6 cards per semester).
     *
     * @param difficulty the selected difficulty
     *
     * @return a GameConfig instance
     */
    public static GameConfig createStandard(final Difficulty difficulty) {
        Objects.requireNonNull(difficulty, "Difficulty cannot be null");
        return new GameConfigImpl(STANDARD_CARDS_PER_SEMESTER, STANDARD_SEMESTERS_PER_GAME, difficulty);
    }

    /**
     * Creates a short configuration for testing (1 semester, 2 choices).
     *
     * @param difficulty the selected difficulty
     *
     * @return a GameConfig instance
     */
    public static GameConfig createShort(final Difficulty difficulty) {
        Objects.requireNonNull(difficulty, "Difficulty cannot be null");
        return new GameConfigImpl(MINIMAL_NUMBER_CARDS, MINIMAL_NUMBER_SEMESTER, difficulty);
    }
}
