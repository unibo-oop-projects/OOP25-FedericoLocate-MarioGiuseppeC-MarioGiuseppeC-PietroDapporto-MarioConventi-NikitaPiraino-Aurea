package it.unibo.aurea.model.api;

import java.util.List;

/**
 * Interface that manages the core game flow.
 */
public interface GameEngine {

    /**
     * Starts the game.
     */
    void start();

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
     * @return list of parameters.
     */
    List<Parameter> getParameters();

    /**
     * @return copy of parameters.
     */
    List<Parameter> getCopyOfParameters();

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

