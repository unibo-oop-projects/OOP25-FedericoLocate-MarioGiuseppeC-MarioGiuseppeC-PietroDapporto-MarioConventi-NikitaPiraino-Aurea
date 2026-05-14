package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.CharacterType;
import it.unibo.aurea.model.api.FollowUp;
import it.unibo.aurea.model.api.OutcomeType;
import it.unibo.aurea.model.api.ParameterType;

/** 
 * Small test to ensure loading cards from .yaml file works.
 */
class CardLoaderTest {
    private static final int MEDIUM_CHANGE = 8;
    private static final int IMPOSSIBLE_OCCURENCE = 2;
    private final Deck cards = new Deck();

    @Test
    void deckLoader() throws IOException {
        final Card first = cards.getAllCards().get(0);
        assertEquals("prof_publish_pressure", first.getId());
        assertEquals(CharacterType.PROFESSOR, first.getCharacter());
        assertEquals(ParameterType.PROFESSORS, first.getRefusal().getEffects().getFirst().getParameter());
        assertEquals("No, quality matters more", first.getRefusal().getAnswer());
        final Card third = cards.getAllCards().get(2);
        assertEquals("prof_cancel_hours", third.getId());
        assertEquals(CharacterType.PROFESSOR, third.getCharacter());
        assertEquals(ParameterType.PROFESSORS, third.getApproval().getEffects().getFirst().getParameter());
        assertEquals(MEDIUM_CHANGE, third.getApproval().getEffects().getFirst().getDelta());
    }

    @Test
    void differentIdCards() throws IOException {
        for (final Card tmp : cards.getAllCards()) {
            int occurence = 0;
            for (final Card current : cards.getAllCards()) {
                if (current.getId().equals(tmp.getId())) {
                    occurence++;
                }
            }
            assertNotEquals(IMPOSSIBLE_OCCURENCE, occurence);
        }
    }

    @Test
    void imageLoader() throws IOException {
        for (final Card c : cards.getAllCards()) {
            assertNotNull(
                getClass().getResource(c.getCharacter().getImagePath()),
                "image not found" + c.getCharacter().getImagePath());
        }
    }

    @Test
    void loadFollowUps() throws IOException {
        final FollowUp firstFU = cards.getAllFollowUps().getFirst();
        assertEquals("prof_apartments_cesena", firstFU.getParentId());
        assertEquals(OutcomeType.APPROVAL, firstFU.getTrigger()); 
    }
}
