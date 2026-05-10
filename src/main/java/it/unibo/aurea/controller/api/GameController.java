package it.unibo.aurea.controller.api;

import java.util.Map;
import java.util.Set;

import it.unibo.aurea.model.api.ParameterType;

/**
 * Represents the Controller of the game in the MVC architecture.
 * It acts as a bridge between the View and the Model (game engine).
 */
public interface GameController {

    /**
     * Starts a new game session.
     */
    void startGame();

    /**
     * Called when the player makes a decision on the current card.
     *
     * @param isApproval {@code true} if the player accepted the proposal, {@code false} if refused
     */
    void makeDecision(boolean isApproval);

    /**
     * Previews the parameters that will be affected by a decision, without altering their actual values.
     * Useful for updating the UI hints (e.g., hovering or dragging a card).
     *
     * @param isApproval true to preview the approval decision, false for the refusal.
     * @return a Set of ParameterType that will be modified by the decision.
     */
    Set<ParameterType> previewDecision(boolean isApproval);

    /**
     * Returns the (parameter, delta) pairs that would
     * be applied by the pending decision.
     * 
     * @param isApproval whether to preview approval or refusal effects
     * @return a map associating each affected parameter with the absolute delta
     */
    Map<ParameterType, Integer> previewDecisionDeltas(boolean isApproval);

    /**
     * Returns the current numeric levels of the four parameters.
     * 
     * @return current levels keyed by parameter type
     */
    Map<ParameterType, Integer> getCurrentParametersLevels();

    /**
     * Returns the player identity collected at login.
     * 
     * @return the player info
     */
    PlayerInfo getPlayerInfo();

    /**
     * Quits the application.
     */
    void quitGame();
}
