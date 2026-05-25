package it.unibo.aurea.model.api;

/**
 * Read-only view of a game parameter. Exposes only query methods, so the View layer
 * cannot accidentally mutate game state.
 */
public interface ParameterView {
    /**
     * @return the current level, between 0 and 100
     */
    int getLevel();

    /**
     * @return true if the parameter is still in a valid range
     */
    boolean isAlive();

    /**
     * @return the name of this parameter
     */
    ParameterType getName();

    /**
     * Registers an observer that is notified whenever the level changes.
     *
     * @param observer the observer to add
     */
    void addObserver(ParameterObserver observer);

    /**
     * @return a human-readable reason why this parameter caused a game over
     */
    String getDeathReason();
}
