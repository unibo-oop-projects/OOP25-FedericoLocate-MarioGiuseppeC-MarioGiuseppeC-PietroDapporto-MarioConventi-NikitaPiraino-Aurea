package it.unibo.aurea.controller;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import it.unibo.aurea.controller.api.GameController;
import it.unibo.aurea.controller.api.PlayerInfo;
import it.unibo.aurea.model.Decision;
import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.Effect;
import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.model.api.GameState;
import it.unibo.aurea.model.api.Parameter;
import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.view.api.GameView;

/**
 * Implementation of the GameController.
 * Manages the flow between the Deck (Model) and the GUI (View).
 */
public final class GameControllerImpl implements GameController {

    // Logger to track the error, so the game doesn't crash
    private static final Logger LOGGER = Logger.getLogger(GameControllerImpl.class.getName());

    private final GameView view;
    private final GameEngine model;
    private final Map<ParameterType, Parameter> parametersMap;
    private final PlayerInfo playerInfo;

    /**
     * Constructor for the controller.
     * Sets up the reactive connection between Model and View.
     *
     * @param view the {@code GameView} to update
     * @param model the {@code GameEngine} used
     * @param playerInfo the player identity collected at login
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "The view must be stored as a reference to allow communication."
    )
    public GameControllerImpl(final GameView view, final GameEngine model, final PlayerInfo playerInfo) {
        this.view = view;
        this.model = model;
        this.playerInfo = Objects.requireNonNull(playerInfo);

        this.parametersMap = model.getParameters().stream()
            .collect(Collectors.toMap(Parameter::getName, p -> p));

        this.parametersMap.values().forEach(p -> p.addObserver(this.view::updateSingleParameter));
    }

    @Override
    public void startGame() {
        LOGGER.info("Starting a new game session...");
        model.start();

        this.parametersMap.values().forEach(p ->
            view.updateSingleParameter(p.getName(), p.getLevel())
        );
        updateUI();
    }

    @Override
    public void makeDecision(final boolean isApproval) {
        final Card currentCard = model.getCurrentCard();

        if (currentCard == null) {
            LOGGER.severe("CRITICAL: Tried to make a decision but currentCard is null!");
            return;
        }

        // Ensure the game is running and a card is active
        if (model.getGameState() == GameState.RUNNING) {
            // Retrieve the chosen decision (Approval or Refusal)
            final Decision decision = isApproval ? currentCard.getApproval() : currentCard.getRefusal();

            if (decision == null || decision.getEffects() == null) {
                LOGGER.warning(() -> "Missing decision data on card: " + currentCard);
            } else {
                // Apply all effects to the corresponding parameters
                for (final Effect effect : decision.getEffects()) {
                    final Parameter p = parametersMap.get(effect.getParameter());
                    if (p != null) {
                        p.modify(effect.getDelta()); // automatically triggers the Observer notification
                    } else {
                        LOGGER.warning(() -> "YAML Error: Unknown parameter requested -> " + effect.getParameter());
                    }
                }
            }

            currentCard.changeUsage();
            model.getGameClock().nextTurn();
            updateUI();
        }
    }

    @Override
    public Set<ParameterType> previewDecision(final boolean isApproval) {
        final Card currentCard = model.getCurrentCard();

        // Fail-safe: if there isn't card or the game is not running, return an empty set
        if (currentCard == null || model.getGameState() != GameState.RUNNING) {
            return Collections.emptySet();
        }

        final Decision decision = isApproval ? currentCard.getApproval() : currentCard.getRefusal();
        if (decision == null || decision.getEffects() == null) {
            return Collections.emptySet();
        }

        // Map the effects to their parameter types and collect them in a Set (to avoid duplicates)
        return decision.getEffects().stream()
            .map(Effect::getParameter)
            .collect(Collectors.toSet());
    }

    @Override
    public Map<ParameterType, Integer> previewDecisionDelta(final boolean isApproval) {
        final Card currentCard = model.getCurrentCard();
        if (currentCard == null || model.getGameState() != GameState.RUNNING) {
            return Collections.emptyMap();
        }

        final Decision decision = isApproval ? currentCard.getApproval() : currentCard.getRefusal();
        if (decision == null || decision.getEffects() == null) {
            return Collections.emptyMap();
        }

        final Map<ParameterType, Integer> result = new EnumMap<>(ParameterType.class);
        for (final Effect effect : decision.getEffects()) {
            final int absDelta = Math.abs(effect.getDelta());
            result.merge(effect.getParameter(), absDelta, Integer::sum);
        }
        return result;
    }

    @Override
    public Map<ParameterType, Integer> getCurrentParametersLevel() {
        final Map<ParameterType, Integer> result = new EnumMap<>(ParameterType.class);
        parametersMap.forEach((type, param) -> result.put(type, param.getLevel()));
        return result;
    }

    @Override
    public PlayerInfo getPlayerInfo() {
        return this.playerInfo;
    }

    /**
     * Synchronizes the View with the current state of the Model.
     */
    private void updateUI() {
        final GameState state = model.getGameState();

        if (state == GameState.RUNNING) {
            view.updateTime(model.getGameClock().getCurrentSemester(), model.getGameClock().getCurrentTurn());
            view.displayCard(model.getCurrentCard());
        } else {
            this.handleGameEnd(state);
        }
    }

    /**
     * Handles the graphic transitions for game termination.
     *
     * @param state the terminal state of the game
     */
    private void handleGameEnd(final GameState state) {
        switch (state) {
            case WON -> {
                LOGGER.info(() -> "Game Over: Player WON!");
                view.showVictory();
            }
            case LOST -> {
                final String reason = determineDefeatReason();
                LOGGER.info(() -> "Game Over: Player LOST. Reason: " + reason);
                view.showGameOver(reason);
            }
            default -> throw new IllegalStateException("Unexpected State: " + state);
        }
    }

    /**
     * Helper method to analyze parameters and find the cause of the defeat.
     *
     * @return a string indicating which parameter reached zero or max capacity.
     */
    private String determineDefeatReason() {
        return parametersMap.values().stream()
            .filter(p -> !p.isAlive())
            .map(Parameter::getDeathReason)
            .findFirst()
            .orElse("Unknown Causes");
    }

    @Override
    public void quitGame() {
        LOGGER.info(() -> "Game terminated safely.");
        // TODO I should understand if this is usable to quit safely with the
        // GUI or to save locally the data, or for now is useless.
    }
}
