package it.unibo.aurea.model.dto;

import java.util.List;

/**
 * Root wrapper for the difficulty configuration YAML file.
 * Maps the top-level {@code difficulty} key to a list of {@link DifficultySettingsDTO}.
 *
 * @param difficulty the list of difficulty level configurations
 */
public record DifficultySettingsFile(List<DifficultySettingsDTO> difficulty) {

    /**
     * Compact constructor that defensively copies the list to ensure immutability.
     *
     * @param difficulty the list of difficulty configurations
     */
    public DifficultySettingsFile {
        difficulty = List.copyOf(difficulty);
    }
}
