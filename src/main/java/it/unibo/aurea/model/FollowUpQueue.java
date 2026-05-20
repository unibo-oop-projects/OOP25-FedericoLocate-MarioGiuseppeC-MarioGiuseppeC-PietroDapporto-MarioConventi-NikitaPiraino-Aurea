package it.unibo.aurea.model;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.FollowUp;
import it.unibo.aurea.model.api.OutcomeType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manages the queue of delayed sub-events (child cards).
 */
public final class FollowUpQueue {

    // Constant declarations must be at the very top of the class
    private static final Logger LOGGER = Logger.getLogger(FollowUpQueue.class.getName());
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
        final Iterator<ActiveFollowUp> iterator = eventQueue.iterator();
        while (iterator.hasNext()) {
            final ActiveFollowUp activeEvent = iterator.next();
            if (activeEvent.getRemainingTurns() <= 0) {
                iterator.remove();

                final Card forcedCard = deck.getAllChildCards().stream()
                        .filter(c -> c.getId().equals(activeEvent.getFollowUp().getChildId()))
                        .findFirst()
                        .orElse(null);

                if (forcedCard == null) {
                    LOGGER.warning("FollowUp child card not found for id: "
                            + activeEvent.getFollowUp().getChildId());
                    continue;
                }

                if (!forcedCard.isUsed()) {
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
