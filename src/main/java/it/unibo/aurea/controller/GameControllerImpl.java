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
 * Manages the flow between the Model and the View.
 */
public final class GameControllerImpl implements GameController {

    private static final Logger LOGGER = Logger.getLogger(GameControllerImpl.class.getName());
    private final GameView view;
    private final GameEngine model;
    private final Map<ParameterType, Parameter> parametersMap;
    private final PlayerInfo playerInfo;

    /**
     * Constructor for the controller.
     * Sets up the reactive connection between Model and View.
     *
     * @param view       the {@code GameView} to update
     * @param model      the {@code GameEngine} used
     * @param playerInfo the player identity collected at login
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "The view must be stored as a reference.")
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
        this.parametersMap.values().forEach(p -> view.updateSingleParameter(p.getName(), p.getLevel()));
        updateUI();
    }

    @Override
    public void makeDecision(final boolean isApproval) {
        final Card currentCard = model.getCurrentCard();
        if (currentCard == null) {
            LOGGER.severe("CRITICAL: currentCard is null!");
            return;
        }

        if (model.getGameState() == GameState.RUNNING) {
            final Decision decision = isApproval ? currentCard.getApproval() : currentCard.getRefusal();

            if (decision == null || decision.getEffects() == null) {
                LOGGER.warning(() -> "Missing decision data on card: " + currentCard);
            } else {
                model.applyEffects(decision.getEffects());
            }

            // Register choice consequences to trigger follow-up events
            model.registerChoiceConsequences(currentCard.getId(), isApproval);

            currentCard.changeUsage();
            model.getGameClock().nextTurn();
            updateUI();
        }
    }

    @Override
    public Set<ParameterType> previewDecision(final boolean isApproval) {
        final Card currentCard = model.getCurrentCard();
        if (currentCard == null || model.getGameState() != GameState.RUNNING) {
            return Collections.emptySet();
        }

        final Decision decision = isApproval ? currentCard.getApproval() : currentCard.getRefusal();
        if (decision == null || decision.getEffects() == null) {
            return Collections.emptySet();
        }

        return decision.getEffects().stream()
                .map(Effect::getParameter)
                .collect(Collectors.toSet());
    }

    @Override
    public Map<ParameterType, Integer> previewDecisionDeltas(final boolean isApproval) {
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
    public Map<ParameterType, Integer> getCurrentParametersLevels() {
        final Map<ParameterType, Integer> result = new EnumMap<>(ParameterType.class);
        parametersMap.forEach((type, param) -> result.put(type, param.getLevel()));
        return result;
    }

    @Override
    public PlayerInfo getPlayerInfo() {
        return this.playerInfo;
    }

    private void updateUI() {
        final GameState state = model.getGameState();
        if (state == GameState.RUNNING) {
            view.updateTime(model.getGameClock().getCurrentSemester(), model.getGameClock().getCurrentTurn());
            view.displayCard(model.getCurrentCard());
        } else {
            this.handleGameEnd(state);
        }
    }

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
        javafx.application.Platform.exit();
    }
}
