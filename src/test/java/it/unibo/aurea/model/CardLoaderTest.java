package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.CharacterType;
import it.unibo.aurea.model.api.FollowUp;
import it.unibo.aurea.model.api.OutcomeType;
import it.unibo.aurea.model.api.ParameterType;

/** 
 * Tests the loading of cards, child cards, follow-up rules,
 * and related resource consistency from YAML configuration files.
 */
class CardLoaderTest {
    private static final int BIG_CHANGE = 15;
    private final Deck cards = new Deck();

    @Test
    void shouldLoadCards() throws IOException {
        final Card first = cards.getAllCards().get(0);
        assertEquals("prof_publish_pressure", first.getId());
        assertEquals(CharacterType.PROFESSOR, first.getCharacter());
        assertEquals(ParameterType.PROFESSORS, first.getRefusal().getEffects().getFirst().getParameter());
        assertEquals("No, quality matters more", first.getRefusal().getAnswer());
        final Card firstChild = cards.getAllChildCards().getFirst();
        assertEquals("bus_realestate_offer", firstChild.getId());
        assertEquals(CharacterType.BUSINESSMAN, firstChild.getCharacter());
        assertEquals(ParameterType.PROFESSORS, firstChild.getApproval().getEffects().getFirst().getParameter());
        assertEquals(BIG_CHANGE, firstChild.getApproval().getEffects().getFirst().getDelta());
    }

    @Test
    void shouldLoadFollowUp() throws IOException {
        final FollowUp firstFU = cards.getAllFollowUps().getFirst();
        assertEquals("prof_apartments_cesena", firstFU.getParentId());
        assertEquals("bus_realestate_offer", firstFU.getChildId());
        assertEquals(OutcomeType.APPROVAL, firstFU.getTrigger()); 
    }

    @Test
    void shouldHaveUniqueIdCards() throws IOException {
        final Set<String> ids = new HashSet<>();
        for (final Card card : cards.getAllCards()) {
            assertTrue(ids.add(card.getId()), "Duplicated main card id:" + card.getId());
        }
    }

    @Test
    void shouldHaveUniqueIdChildCards() throws IOException {
        final Set<String> ids = new HashSet<>();
        for (final Card card : cards.getAllChildCards()) {
            assertTrue(ids.add(card.getId()), "Duplicated child card id:" + card.getId());
        }
    }

    @Test
    void shouldNotBeSharedIds() throws IOException {
        final Set<String> mainCardIds = cards.getAllCards().stream()
            .map(Card::getId)
            .collect(Collectors.toSet());
        for (final Card childCard : cards.getAllChildCards()) {
            assertFalse(
                mainCardIds.contains(childCard.getId()),
                "Child card id already used by main card: " + childCard.getId()
            );
        }
    }

    @Test
    void shouldCharacterImageExist() throws IOException {
        final List<Card> allCards = Stream.concat(
            this.cards.getAllCards().stream(), 
            this.cards.getAllChildCards().stream()
        ).toList();
        for (final Card c : allCards) {
            assertNotNull(
                getClass().getResource(c.getCharacter().getImagePath()),
                "image not found" + c.getCharacter().getImagePath());
        }
    }

}
