package it.unibo.aurea.model.dto;

import it.unibo.aurea.model.api.Difficulty;

/**
 * DTO representing a single difficulty level and its associated balancing parameters.
 *
 * @param level         the difficulty level (EASY, NORMAL, HARD)
 * @param weightDivisor the divisor applied to the adaptive boost in card selection;
 *                      higher values suppress the boost, making selection closer to random
 */
public record DifficultySettingsDTO(
    Difficulty level,
    double weightDivisor) {
}
