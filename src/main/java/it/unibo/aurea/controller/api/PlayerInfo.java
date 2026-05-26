package it.unibo.aurea.controller.api;

import java.util.Objects;

import it.unibo.aurea.model.api.Difficulty;

/**
 * Immutable value object holding the player's identity collected at login.
 *
 * @param rectorName the name typed by the player at login
 * @param faculty the faculty the rector is associated with
 * @param difficulty the selected game difficulty
 */
public record PlayerInfo(String rectorName, String faculty, Difficulty difficulty) {

    /**
     * Compact constructor with null validation.
     * 
     * @throws NullPointerException if any parameter is null
     */
    public PlayerInfo {
        Objects.requireNonNull(rectorName, "rectorName must not be null");
        Objects.requireNonNull(faculty, "faculty must not be null");
        Objects.requireNonNull(difficulty, "difficulty must not be null");
    }
}
