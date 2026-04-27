package it.unibo.aurea.model.api;

/**
 * Represents the structure of the four parameter of the game.
 * 
 */
public interface Parameter {
    /**
     * Indicates the level of the current parameter.
     * 
     * @return an {@code int} between 0/100
     */
    int getLevel();

    /**
     * Performs the raises or decreases of the level of this parameter. 
     * Based on the level it can modify the life of this parameter.
     * 
     * @param delta the amount to add/subtract based on the sign.
     */
    void modify(int delta);

    /**
     * Indicates if the player can continue the game based on this parameter.
     * 
     * @return {@code true} if is > 0 && < 100, {@code false} otherwise
     */
    boolean isAlive();

    /**
     * Indicates the name of the current parameter.
     * 
     * @return the name of the parameter 
     */
    ParameterType getName();

    /**
     * Adds an observer that will be notified of any changes to this parameter.
     *
     * @param observer the {@code ParameterObserver} to add
     */
    void addObserver(ParameterObserver observer);

    /**
     * Retrieves the specific reason for the parameter's depletion or saturation.
     *
     * @return a String detailing the cause of game over related to this parameter.
     */
    String getDeathReason();
}

