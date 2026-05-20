package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.FollowUp;
import it.unibo.aurea.model.api.OutcomeType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Manages the queue of delayed sub-events (child cards).
 */
public final class FollowUpQueue {
    private final List<ActiveFollowUp> eventQueue = new ArrayList<>();

    /**
     * Decrements the remaining turns for all active follow-ups.
     */
    public void updateTurns() {
        final Iterator<ActiveFollowUp> iterator = eventQueue.iterator();
        while (iterator.hasNext()) {
            final ActiveFollowUp event = iterator.next();
            event.decrementTurn();
        }
    }

    /**
     * Checks if there is a forced child card ready to be played.
     * 
     * @param deck the game deck
     * @return an Optional containing the forced card, or empty
     */
    public Optional<Card> pollForcedCard(final Deck deck) {
        for (final ActiveFollowUp activeEvent : eventQueue) {
            if (activeEvent.getRemainingTurns() <= 0) {
                final Card forcedCard = deck.getAllCards().stream()
                        .filter(c -> c.getId().equals(activeEvent.getFollowUp().getChildId()))
                        .findFirst()
                        .orElse(null);
                if (forcedCard != null && !forcedCard.isUsed()) {
                    eventQueue.remove(activeEvent);
                    return Optional.of(forcedCard);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Registers a new follow-up event based on the player's choice.
     * 
     * @param deck        the game deck
     * @param parentId    the id of the played card
     * @param wasApproval true if approved, false if refused
     */
    public void registerConsequences(final Deck deck, final String parentId, final boolean wasApproval) {
        final OutcomeType actualOutcome = wasApproval ? OutcomeType.APPROVAL : OutcomeType.REFUSAL;

        deck.getAllFollowUps().stream()
                .filter(fu -> fu.getParentId().equals(parentId))
                .filter(fu -> fu.getTrigger() == actualOutcome)
                .forEach(fu -> eventQueue.add(new ActiveFollowUp(fu, fu.getDelayTurn())));
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
