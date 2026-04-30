package it.unibo.aurea.view.api;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.ParameterType;

/**
 * Represents the main interface for the game's View.
 * It defines how the system shows outputs to the player.
 */
public interface GameView {

    /**
     * Updates the UI to show a new card to the player.
     *
     * @param card the {@code Card} to display
     */
    void displayCard(Card card);

    /**
     * Updates the UI for a single specific parameter.
     * 
     * @param type the type of the parameter to update
     * @param newValue the new value to display
     */
    void updateSingleParameter(ParameterType type, int newValue);

    /**
     * Displays the Game Over screen.
     *
     * @param message the reason of the game over
     */
    void showGameOver(String message);

    /**
     * this method handles the graphic of the victory situation.
     */
    void showVictory();

    /**
     * this method handles the graphic of the Game over situation.
     */
    void showDefeat();

    /**
     * Links the Controller to the View so the View can send user inputs (like button clicks).
     * 
     * @param controller the GameController.
     */
    void setController(it.unibo.aurea.controller.api.GameController controller);

    /**
     * Updates the time counter on the UI.
     * 
     * @param semester the current semester/session
     * @param turn the current turn
     */
    void updateTime(int semester, int turn);
}
