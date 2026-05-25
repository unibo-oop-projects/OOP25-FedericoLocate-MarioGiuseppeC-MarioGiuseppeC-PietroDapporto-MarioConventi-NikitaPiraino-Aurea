package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Difficulty;
import it.unibo.aurea.model.api.GameConfig;

/**
 * Factory dedicated to creating game configurations.
 * Centralizes initialization logic by hiding the concrete implementation class.
 */
public final class GameConfigFactory {
    /**
     * Creates a standard configuration (3 years, 6 cards per semester).
     *
     * @param difficulty the selected difficulty
     *
     * @return a GameConfig instance
     */
    public static GameConfig createStandard(final Difficulty difficulty) {
        return new GameConfigImpl(6, 6, difficulty);
    }

    /**
     * Creates a short configuration for testing (1 semester, 2 choices).
     *
     * @param difficulty the selected difficulty
     *
     * @return a GameConfig instance
     */
    public static GameConfig createShort(final Difficulty difficulty) {
        return new GameConfigImpl(2, 2, difficulty);
    }
}
