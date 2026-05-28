package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.Effect;
import it.unibo.aurea.model.api.GameClock;
import it.unibo.aurea.model.api.GameConfig;
import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.model.api.GameState;
import it.unibo.aurea.model.api.Parameter;
import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.model.api.ParameterView;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of the GameEngine. Orchestrates sub-components adhering to
 * SRP.
 */
public final class GameEngineImpl implements GameEngine {

    private final Deck deck;
    private final GameConfig config;
    private final GameClock gameClock;
    private final List<Parameter> parameters;
    private Card currentCardToPlay;

    // Specialized sub-components (Composition / SRP)
    private final DifficultySettings difficultySettings;
    private final FollowUpQueue followUpQueue;
    private final CardSelector cardSelector;

    /**
     * @param config the configuration
     * @param deck   the deck
     * @param gameClock the time manager fo the game.
     */
    public GameEngineImpl(final GameConfig config, final GameClock gameClock, final Deck deck) {
        this.config = Objects.requireNonNull(config, "GameConfig cannot be null");
        this.gameClock = Objects.requireNonNull(gameClock, "GameClock cannot be null");
        this.deck = Objects.requireNonNull(deck, "Deck cannot be null");

        // Initialization of specialized components
        this.difficultySettings = new DifficultySettings(config.getDifficulty());
        this.followUpQueue = new FollowUpQueue();
        this.cardSelector = new CardSelector();

        // Parameters always start at default value (50)
        this.parameters = List.of(
                new ParameterImpl(ParameterType.FINANCES),
                new ParameterImpl(ParameterType.STUDENTS),
                new ParameterImpl(ParameterType.PROFESSORS),
                new ParameterImpl(ParameterType.REPUTATION));

        this.currentCardToPlay = extractNextCard();
    }

    @Override
    public GameConfig getGameConfig() {
        return config;
    }

    @Override
    public boolean isTimeFinished() {
        return gameClock.isTimeFinished();
    }

    @Override
    public Card getCurrentCard() {
        if (this.currentCardToPlay == null || this.currentCardToPlay.isUsed()) {
            this.currentCardToPlay = extractNextCard();
        }
        return this.currentCardToPlay;
    }

    private Card extractNextCard() {
        // 1. First, extract the forced card if its timer has expired
        final Optional<Card> forcedCard = this.followUpQueue.pollForcedCard(this.deck);
        if (forcedCard.isPresent()) {
            return forcedCard.get();
        }

        // 2. Decrement the turns for the next cycle
        this.followUpQueue.updateTurns();

        // 3. Standard card selection
        return this.cardSelector.selectNextCard(this.deck, this.parameters, this.difficultySettings);
    }

    @Override
    public void registerChoiceConsequences(final String parentId, final boolean wasApproval) {
        // Complete delegation to the FollowUpQueue component
        this.followUpQueue.registerConsequences(this.deck, parentId, wasApproval);
    }

    @Override
    public void applyEffects(final List<Effect> effects) {
        // Iterates through the effects and updates the corresponding parameters using
        // modify()
        for (final Effect effect : effects) {
            for (final Parameter p : parameters) {
                if (p.getName() == effect.getParameter()) {
                    p.modify(effect.getDelta());
                }
            }
        }
    }

    @Override
    public List<ParameterView> getParameters() {
        return List.copyOf(this.parameters);
    }

    @Override
    public GameState getGameState() {
        if (!areAllParametersAlive()) {
            return GameState.LOST;
        }
        if (gameClock.isTimeFinished()) {
            return GameState.WON;
        }
        return GameState.RUNNING;
    }

    private boolean areAllParametersAlive() {
        return parameters.stream().allMatch(Parameter::isAlive);
    }

    @Override
    public GameClock getGameClock() {
        return this.gameClock;
    }
}
