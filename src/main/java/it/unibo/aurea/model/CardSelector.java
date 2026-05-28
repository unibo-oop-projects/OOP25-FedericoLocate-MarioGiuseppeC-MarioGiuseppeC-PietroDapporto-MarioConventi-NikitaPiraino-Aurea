package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.Effect;
import it.unibo.aurea.model.api.ParameterType;
import it.unibo.aurea.model.api.ParameterView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles the weighted probabilistic selection of the next base card.
 */
public final class CardSelector {

    private static final double DEFAULT_WEIGHT = 10.0;
    private static final int NEUTRAL_DISTANCE = 50;

    private final Random randomGenerator = new Random();

    /**
     * Selects the next card from the deck based on parameters state and difficulty
     * settings.
     *
     * @param deck               the game deck
     * @param parameters         the current game parameters
     * @param difficultySettings the difficulty settings for scaling weights
     * @return the selected Card
     */
    public Card selectNextCard(final Deck deck, final List<? extends ParameterView> parameters,
            final DifficultySettings difficultySettings) {
        ParameterType criticalParam = ParameterType.FINANCES;
        int minDistance = NEUTRAL_DISTANCE;

        for (final ParameterView p : parameters) {
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
            if (!c.isUsed() && isBaseCard(deck, c.getId()) && !isLethalInBothOptions(c, parameters)) {
                playableCards.add(c);
                double weight = DEFAULT_WEIGHT;
                if (cardHelpsParameter(c, criticalParam, parameters)) {
                    weight *= 1.0 + (NEUTRAL_DISTANCE - minDistance) / difficultySettings.getWeightDivisor();
                }
                weights.add(weight);
                totalWeight += weight;
            }
        }

        // Security-Failback
        if (playableCards.isEmpty()) {
            return deck.getAllCards().stream()
                    .filter(c -> !c.isUsed())
                    .findFirst()
                    .orElseGet(() -> {
                        deck.getAllCards().forEach(Card::changeUsage);
                        return deck.getAllCards().get(0);
                    });
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

    private boolean isBaseCard(final Deck deck, final String id) {
        return deck.getAllFollowUps().stream().noneMatch(fu -> fu.getChildId().equals(id));
    }

    private boolean isLethalInBothOptions(final Card card, final List<? extends ParameterView> parameters) {
        return simulateLethality(card.getApproval().getEffects(), parameters)
                && simulateLethality(card.getRefusal().getEffects(), parameters);
    }

    private boolean simulateLethality(final List<Effect> effects, final List<? extends ParameterView> parameters) {
        for (final Effect e : effects) {
            for (final ParameterView p : parameters) {
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

    private boolean cardHelpsParameter(final Card card, final ParameterType type,
            final List<? extends ParameterView> parameters) {
        final ParameterView criticalP = parameters.stream()
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
}
