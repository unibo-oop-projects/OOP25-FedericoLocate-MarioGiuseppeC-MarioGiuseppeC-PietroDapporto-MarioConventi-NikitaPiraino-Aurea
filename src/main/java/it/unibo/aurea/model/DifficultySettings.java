package it.unibo.aurea.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import it.unibo.aurea.model.api.Difficulty;
import it.unibo.aurea.model.dto.DifficultySettingsDTO;
import it.unibo.aurea.model.dto.DifficultySettingsFile;

/**
 * Encapsulates balancing settings based on game difficulty.
 * Parameters are loaded from the external {@code difficulty.yaml} configuration file,
 * following the same pattern used by {@link Deck} for cards and follow-ups.
 * This eliminates hardcoded constants and makes balancing configurable without recompilation.
 */
public final class DifficultySettings {

    private static final String CONFIG_FILE = "difficulty.yaml";

    private final double weightDivisor;

    /**
     * Loads difficulty parameters from the YAML configuration file and selects
     * the entry matching the given difficulty level.
     *
     * @param difficulty the selected game difficulty
     * @throws IllegalStateException if the config file is missing, unreadable,
     *                               or does not contain an entry for the given difficulty
     */
    public DifficultySettings(final Difficulty difficulty) {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final InputStream input = DifficultySettings.class
                .getClassLoader()
                .getResourceAsStream(CONFIG_FILE);

        if (Objects.isNull(input)) {
            throw new IllegalStateException("Unable to find " + CONFIG_FILE);
        }

        final DifficultySettingsFile settingsFile;
        try {
            settingsFile = mapper.readValue(input, DifficultySettingsFile.class);
        } catch (final IOException e) {
            throw new IllegalStateException(
                    "Failed to load " + CONFIG_FILE + ": " + e.getMessage(), e);
        }

        this.weightDivisor = settingsFile.difficulty().stream()
                .filter(dto -> dto.level() == difficulty)
                .map(DifficultySettingsDTO::weightDivisor)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No difficulty entry found for level: " + difficulty));
    }

    /**
     * @return the weight divisor used to modulate the adaptive boost in card selection
     */
    public double getWeightDivisor() {
        return this.weightDivisor;
    }
}
