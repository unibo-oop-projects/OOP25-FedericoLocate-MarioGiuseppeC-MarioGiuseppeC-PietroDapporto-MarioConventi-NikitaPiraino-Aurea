package it.unibo.aurea.model.dto;

import it.unibo.aurea.model.api.Difficulty;

/**
 * DTO representing a single difficulty level and its associated balancing parameters.
 *
 * @param level           the difficulty level (EASY, NORMAL, HARD)
 * @param weightDivisor   the divisor applied to the adaptive boost in card selection;
 *                        higher values suppress the boost, making selection closer to random
 * @param deltaMultiplier the multiplier applied to every card-effect delta before it is
 *                        applied to a parameter; values below 1.0 soften impacts (EASY),
 *                        1.0 is baseline (NORMAL), values above 1.0 amplify impacts (HARD)
 */
public record DifficultySettingsDTO(
    Difficulty level,
    double weightDivisor,
    double deltaMultiplier) {
}
