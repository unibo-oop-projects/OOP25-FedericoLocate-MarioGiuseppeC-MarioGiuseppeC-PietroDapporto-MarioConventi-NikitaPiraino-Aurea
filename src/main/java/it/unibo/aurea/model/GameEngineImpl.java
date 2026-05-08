package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.Effect;
import it.unibo.aurea.model.api.FollowUp;
import it.unibo.aurea.model.api.GameClock;
import it.unibo.aurea.model.api.GameConfig;
import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.model.api.GameState;
import it.unibo.aurea.model.api.Parameter;
import it.unibo.aurea.model.api.ParameterType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Implementation of the GameEngine.
 */
public final class GameEngineImpl implements GameEngine {

    private static final double DEFAULT_WEIGHT = 10.0;
    private static final double WEIGHT_DIVISOR = 25.0;
    private static final int NEUTRAL_DISTANCE = 50;

    private final Deck deck;
    private final GameConfig config;
    private final GameClock gameClock;
    private final Random randomGenerator;
    private final List<ActiveFollowUp> eventQueue = new ArrayList<>();
    private Card currentCardToPlay;

    private final List<Parameter> parameters = List.of(
        new ParameterImpl(ParameterType.FINANCES),
        new ParameterImpl(ParameterType.STUDENTS),
        new ParameterImpl(ParameterType.PROFESSORS),
        new ParameterImpl(ParameterType.REPUTATION)
    );

    /**
     * @param config the configuration
     * @param deck the deck
     */
    public GameEngineImpl(final GameConfig config, final Deck deck) {
        this.config = config;
        this.gameClock = new GameClockImpl(config);
        this.deck = Objects.requireNonNull(deck, "Deck cannot be null");
        this.randomGenerator = new Random();
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
    public void start() {
        // Init if needed
    }

    @Override
    public Card getCurrentCard() {
        if (this.currentCardToPlay == null || this.currentCardToPlay.isUsed()) {
            this.currentCardToPlay = extractNextCard();
        }
        return this.currentCardToPlay;
    }

    private Card extractNextCard() {
        updateEventQueue();
        for (final ActiveFollowUp activeEvent : eventQueue) {
            if (activeEvent.getRemainingTurns() <= 0) {
                final Card forcedCard = deck.getAllCards().stream()
                    .filter(c -> c.getId().equals(activeEvent.getFollowUp().getChildId()))
                    .findFirst()
                    .orElse(null);
                if (forcedCard != null && !forcedCard.isUsed()) {
                    eventQueue.remove(activeEvent);
                    return forcedCard;
                }
            }
        }

        ParameterType criticalParam = ParameterType.FINANCES;
        int minDistance = NEUTRAL_DISTANCE;

        for (final Parameter p : parameters) {
            final int dist0 = p.getLevel();
            final int dist100 = 100 - p.getLevel();
            final int currentMinDist = Math.min(dist0, dist100);
            if (currentMinDist < minDistance) {
                minDistance = currentMinDist;
                criticalParam = p.getName();
            }
        }

        final List<Card> playableCards = new ArrayList<>();
        final List<Double> weights = new ArrayList<>();
        double totalWeight = 0.0;

        for (final Card c : deck.getAllCards()) {
            if (!c.isUsed() && isBaseCard(c.getId()) && !isLethalInBothOptions(c)) {
                playableCards.add(c);
                double weight = DEFAULT_WEIGHT;
                if (cardHelpsParameter(c, criticalParam)) {
                    weight *= 1.0 + (NEUTRAL_DISTANCE - minDistance) / WEIGHT_DIVISOR;
                }
                weights.add(weight);
                totalWeight += weight;
            }
        }

        if (playableCards.isEmpty()) {
            return deck.getAllCards().stream()
                .filter(c -> !c.isUsed())
                .findFirst()
                .orElse(deck.getAllCards().get(0));
        }

        final double randomVal = randomGenerator.nextDouble() * totalWeight;
        double currentSum = 0;
        for (int i = 0; i < playableCards.size(); i++) {
            currentSum += weights.get(i);
            if (randomVal <= currentSum) {
                return playableCards.get(i);
            }
        }
        return playableCards.get(0);
    }

    private boolean isBaseCard(final String id) {
        return deck.getAllFollowUps().stream().noneMatch(fu -> fu.getChildId().equals(id));
    }

    private boolean isLethalInBothOptions(final Card card) {
        return simulateLethality(card.getApproval().getEffects())
            && simulateLethality(card.getRefusal().getEffects());
    }

    private boolean simulateLethality(final List<Effect> effects) {
        for (final Effect e : effects) {
            for (final Parameter p : parameters) {
                if (p.getName() == e.getParameter()) {
                    final int futureValue = p.getLevel() + e.getDelta();
                    if (futureValue <= 0 || futureValue >= 100) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean cardHelpsParameter(final Card card, final ParameterType type) {
        final Parameter criticalP = parameters.stream()
            .filter(p -> p.getName() == type)
            .findFirst()
            .orElse(null);
        if (criticalP == null) {
            return false;
        }
        final boolean isDangerHigh = criticalP.getLevel() > NEUTRAL_DISTANCE;
        return card.getAllEffects().stream()
            .anyMatch(e -> e.getParameter() == type
                && (isDangerHigh ? e.getDelta() < 0 : e.getDelta() > 0));
    }

    private void updateEventQueue() {
        final Iterator<ActiveFollowUp> iterator = eventQueue.iterator();
        while (iterator.hasNext()) {
            final ActiveFollowUp event = iterator.next();
            event.decrementTurn();
        }
    }

    @Override
    public void registerChoiceConsequences(final String parentId, final boolean wasApproval) {
         final it.unibo.aurea.model.api.OutcomeType actualOutcome = wasApproval
             ? it.unibo.aurea.model.api.OutcomeType.APPROVAL
             : it.unibo.aurea.model.api.OutcomeType.REFUSAL;

         deck.getAllFollowUps().stream()
            .filter(fu -> fu.getParentId().equals(parentId))
            .filter(fu -> fu.getTrigger() == actualOutcome)
            .forEach(fu -> eventQueue.add(new ActiveFollowUp(fu, fu.getDelayTurn())));
    }

    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    @Override
    public List<Parameter> getCopyOfParameters() {
        return List.copyOf(parameters);
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

    private static class ActiveFollowUp {
        private final FollowUp followUp;
        private int remainingTurns;

        ActiveFollowUp(final FollowUp followUp, final int remainingTurns) {
            this.followUp = followUp;
            this.remainingTurns = remainingTurns;
        }

        void decrementTurn() {
            this.remainingTurns--;
        }

        int getRemainingTurns() {
            return remainingTurns;
        }

        FollowUp getFollowUp() {
            return followUp;
        }
    }
}
