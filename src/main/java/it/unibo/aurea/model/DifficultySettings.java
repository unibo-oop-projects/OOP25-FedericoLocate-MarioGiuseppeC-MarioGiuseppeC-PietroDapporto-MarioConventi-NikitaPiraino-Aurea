package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Difficulty;

/**
 * Encapsulates balancing settings based on game difficulty.
 */
public final class DifficultySettings {
    private static final double EASY_WEIGHT_DIVISOR = 25.0;
    private static final double NORMAL_WEIGHT_DIVISOR = 50.0;
    private static final double HARD_WEIGHT_DIVISOR = 500.0;

    private final double weightDivisor;

    /**
     * @param difficulty the selected game difficulty
     */
    public DifficultySettings(final Difficulty difficulty) {
        if (difficulty == Difficulty.HARD) {
            this.weightDivisor = HARD_WEIGHT_DIVISOR;
        } else if (difficulty == Difficulty.NORMAL) {
            this.weightDivisor = NORMAL_WEIGHT_DIVISOR;
        } else {
            this.weightDivisor = EASY_WEIGHT_DIVISOR;
        }
    }

    /**
     * @return the weight divisor for card selection
     */
    public double getWeightDivisor() {
        return this.weightDivisor;
    }
}
