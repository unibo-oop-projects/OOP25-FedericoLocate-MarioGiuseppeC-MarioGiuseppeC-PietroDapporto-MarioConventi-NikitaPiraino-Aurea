package it.unibo.aurea.model.api;

import java.util.List;

/**
 * Interface that manages the core game flow.
 */
public interface GameEngine {

    /**
     * Checks if time is finished.
     *
     * @return true if time is over.
     */
    boolean isTimeFinished();

    /**
     * @return active configuration.
     */
    GameConfig getGameConfig();

    /**
     * @return current card.
     */
    Card getCurrentCard();

    /**
     * @return read-only view of the current parameters (safe to pass to the View layer).
     */
    List<ParameterView> getParameters();

    /**
     * @return game clock.
     */
    GameClock getGameClock();

    /**
     * @return game state.
     */
    GameState getGameState();

    /**
     * Registers consequences.
     *
     * @param parentId the ID of the card
     * @param wasApproval true if approved
     */
    void registerChoiceConsequences(String parentId, boolean wasApproval);

    /**
     * Applies the given list of effects to the game parameters, 
     * considering the current difficulty multiplier.
     *
     * @param effects the list of effects to apply
     */
    void applyEffects(List<Effect> effects);
}

