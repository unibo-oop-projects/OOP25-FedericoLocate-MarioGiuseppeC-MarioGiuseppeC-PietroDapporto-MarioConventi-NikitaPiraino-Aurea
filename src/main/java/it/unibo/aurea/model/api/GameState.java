package it.unibo.aurea.model.api;

/**
 * Represents the current state of the game.
 */
public enum GameState {
    RUNNING, //the player isn't already in a terminal condition.
    WON, //the player has won
    LOST, //the player has lost
}
